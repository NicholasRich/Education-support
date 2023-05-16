package com.cniao.test_application.ui.ViewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.cniao.test_application.ui.Repository.Model.User
import com.example.projectmanager.repository.Repository

class UserViewModel : ViewModel(), DefaultLifecycleObserver {
    private val repository = Repository()

    fun signOut() = repository.signOut()

    val users = repository.onUsersChange()

    fun getUserByName(name: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) =
        repository.getUserByName(name, onSuccessAction, onFailureAction)

    fun getUserByEmail(email: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) =
        repository.getUserByEmail(email, onSuccessAction, onFailureAction)

    fun addUser(
        userName: String = "",
        identity: String = "",
        email: String = "",
        notification: String = "",
        previousNotification: String = "",
        onSuccessAction: () -> Unit,
        onFailureAction: () -> Unit
    ) = repository.addUser(User(userName, identity, email, notification, previousNotification), onSuccessAction, onFailureAction)

    fun updateUserProfile(
        user: String,
        key: String,
        info: String,
        onSuccessAction: () -> Unit = {},
        onFailureAction: () -> Unit = {},
    ) = repository.updateUser(user, key, info, onSuccessAction, onFailureAction)

    fun updateUserNotification(
        user: String,
        notification: String,
        onSuccessAction: () -> Unit = {},
        onFailureAction: () -> Unit = {},
    ) = repository.updateUser(user, "notification", notification, onSuccessAction, onFailureAction)

    fun updateUserPreviousNotification(
        user: String,
        previousNotification: String,
        onSuccessAction: () -> Unit = {},
        onFailureAction: () -> Unit = {},
    ) = repository.updateUser(user, "previousNotification", previousNotification, onSuccessAction, onFailureAction)

    fun deleteUser(
        name: String,
        onSuccessAction: () -> Unit,
        onFailureAction: () -> Unit
    ) = repository.deleteUser(name, onSuccessAction, onFailureAction)

}