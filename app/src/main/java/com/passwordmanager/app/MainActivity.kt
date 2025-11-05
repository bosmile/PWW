package com.passwordmanager.app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.passwordmanager.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: PasswordDatabase
    private lateinit var adapter: PasswordAdapter
    private lateinit var encryptionHelper: EncryptionHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Khởi tạo
        database = PasswordDatabase.getDatabase(this)
        encryptionHelper = EncryptionHelper(this)
        
        // Setup RecyclerView
        adapter = PasswordAdapter(
            onEdit = { password -> showEditDialog(password) },
            onDelete = { password -> deletePassword(password) },
            onCopy = { text -> copyToClipboard(text) },
            encryptionHelper = encryptionHelper
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        
        // Load passwords
        loadPasswords()
        
        // Nút thêm password
        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }
    
    private fun loadPasswords() {
        lifecycleScope.launch {
            database.passwordDao().getAllPasswords().collect { passwords ->
                adapter.submitList(passwords)
                
                // Hiển thị empty state
                if (passwords.isEmpty()) {
                    binding.emptyView.visibility = android.view.View.VISIBLE
                    binding.recyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyView.visibility = android.view.View.GONE
                    binding.recyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
    
    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.titleInput)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.usernameInput)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.passwordInput)
        val websiteInput = dialogView.findViewById<TextInputEditText>(R.id.websiteInput)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Thêm mật khẩu mới")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val title = titleInput.text.toString()
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()
                val website = websiteInput.text.toString()
                
                if (title.isNotEmpty() && password.isNotEmpty()) {
                    val encryptedPassword = encryptionHelper.encrypt(password)
                    val newPassword = Password(
                        title = title,
                        username = username,
                        password = encryptedPassword,
                        website = website
                    )
                    
                    lifecycleScope.launch {
                        database.passwordDao().insert(newPassword)
                        Toast.makeText(this@MainActivity, "Đã lưu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun showEditDialog(password: Password) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.titleInput)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.usernameInput)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.passwordInput)
        val websiteInput = dialogView.findViewById<TextInputEditText>(R.id.websiteInput)
        
        // Điền dữ liệu hiện tại
        titleInput.setText(password.title)
        usernameInput.setText(password.username)
        passwordInput.setText(encryptionHelper.decrypt(password.password))
        websiteInput.setText(password.website)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Chỉnh sửa mật khẩu")
            .setView(dialogView)
            .setPositiveButton("Cập nhật") { _, _ ->
                val newPassword = password.copy(
                    title = titleInput.text.toString(),
                    username = usernameInput.text.toString(),
                    password = encryptionHelper.encrypt(passwordInput.text.toString()),
                    website = websiteInput.text.toString()
                )
                
                lifecycleScope.launch {
                    database.passwordDao().update(newPassword)
                    Toast.makeText(this@MainActivity, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun deletePassword(password: Password) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa \"${password.title}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    database.passwordDao().delete(password)
                    Toast.makeText(this@MainActivity, "Đã xóa!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("password", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Đã sao chép!", Toast.LENGTH_SHORT).show()
    }
}
