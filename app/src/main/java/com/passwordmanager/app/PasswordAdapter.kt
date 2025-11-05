package com.passwordmanager.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.passwordmanager.app.databinding.ItemPasswordBinding

class PasswordAdapter(
    private val onEdit: (Password) -> Unit,
    private val onDelete: (Password) -> Unit,
    private val onCopy: (String) -> Unit,
    private val encryptionHelper: EncryptionHelper
) : ListAdapter<Password, PasswordAdapter.PasswordViewHolder>(PasswordDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val binding = ItemPasswordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PasswordViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class PasswordViewHolder(
        private val binding: ItemPasswordBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(password: Password) {
            binding.apply {
                titleText.text = password.title
                usernameText.text = if (password.username.isNotEmpty()) 
                    password.username else "Không có username"
                websiteText.text = if (password.website.isNotEmpty()) 
                    password.website else "Không có website"
                
                // Ẩn/hiện mật khẩu
                var isPasswordVisible = false
                passwordText.text = "••••••••"
                
                btnTogglePassword.setOnClickListener {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        val decrypted = encryptionHelper.decrypt(password.password)
                        passwordText.text = decrypted
                        btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
                    } else {
                        passwordText.text = "••••••••"
                        btnTogglePassword.setImageResource(R.drawable.ic_visibility)
                    }
                }
                
                // Copy mật khẩu
                btnCopy.setOnClickListener {
                    val decrypted = encryptionHelper.decrypt(password.password)
                    onCopy(decrypted)
                }
                
                // Chỉnh sửa
                btnEdit.setOnClickListener {
                    onEdit(password)
                }
                
                // Xóa
                btnDelete.setOnClickListener {
                    onDelete(password)
                }
            }
        }
    }
    
    class PasswordDiffCallback : DiffUtil.ItemCallback<Password>() {
        override fun areItemsTheSame(oldItem: Password, newItem: Password): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Password, newItem: Password): Boolean {
            return oldItem == newItem
        }
    }
}
