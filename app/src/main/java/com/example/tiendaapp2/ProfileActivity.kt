package com.example.tiendaapp2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private lateinit var profileImageView: ImageView
    private lateinit var emailTextView: TextView
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abre la cámara
            openCamera()
        } else {
            // Permiso denegado
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImage)
        emailTextView = findViewById(R.id.emailTextView)

        // Mostrar el correo del usuario autenticado
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            emailTextView.text = it.email
        }

        // Cargar la imagen de perfil desde Firebase Storage
        loadProfileImage()

        // Configurar el lanzador para capturar fotos
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let {
                    profileImageView.setImageURI(it)
                    uploadImageToFirebase(it)
                } ?: run {
                    Toast.makeText(this, "Error al capturar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configurar el lanzador para seleccionar imágenes de la galería
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    profileImageView.setImageURI(imageUri)
                    uploadImageToFirebase(imageUri)
                } else {
                    Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botón para seleccionar imagen de la galería
        findViewById<Button>(R.id.uploadImageButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        }

        // Botón para tomar una foto con la cámara
        findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            // Verifica si ya tienes el permiso de cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // Solicita el permiso si no se ha concedido
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Abre la cámara para tomar una foto
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePhotoLauncher.launch(takePictureIntent)
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_", /* prefijo */
            ".jpg", /* sufijo */
            storageDir /* directorio */
        )
    }

    private fun uploadImageToFirebase(image: Any) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${FirebaseAuth.getInstance().currentUser?.uid}")
        val uploadTask = when (image) {
            is Bitmap -> {
                val baos = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                storageRef.putBytes(data)
            }
            is Uri -> storageRef.putFile(image)
            else -> return
        }

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUriToFirestore(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUriToFirestore(uri: String) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Crear o actualizar el documento del usuario
            val userData = hashMapOf(
                "profileImageUrl" to uri,
                "email" to it.email
            )

            db.collection("users").document(it.uid).set(userData, SetOptions.merge()) // Usamos set() para crear el documento si no existe, merge para no sobrescribir datos existentes
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar la URL de la imagen", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadProfileImage() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Cargar la imagen en el ImageView usando Glide
                            Glide.with(this)
                                .load(profileImageUrl)
                                .error(R.drawable.default_profile_image) // Imagen de respaldo en caso de error
                                .into(profileImageView)
                        } else {
                            // Si no hay URL de imagen, mostrar imagen predeterminada
                            profileImageView.setImageResource(R.drawable.default_profile_image)
                        }
                    } else {
                        // Si no existe el documento, lo creamos con datos predeterminados
                        saveImageUriToFirestore("") // Crear el documento vacío
                        Toast.makeText(this, "Documento de usuario creado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar imagen de perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }



}



