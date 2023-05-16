package com.example.projectmanager.repository

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.cniao.test_application.ui.Repository.Model.Project
import com.cniao.test_application.ui.Repository.Model.Task
import com.cniao.test_application.ui.Repository.Model.User


private const val USER_COLLECTION = "users"
private const val USER_EMAIL_KEY = "email"
private const val USER_PASSWORD_KEY = "email"
private const val PROJECT_COLLECTION = "projects"
private const val TASK_COLLECTION = "tasks"
private const val PROJECT_MANAGER_KEY = "manager"
private const val MEMBERS_KEY = "members"
private const val PROJECT_KEY = "project"
private const val PROJECT_NAME_KEY = "name"
private const val PROJECT_MEMBERS_KEY = "members"
private const val PROJECT_TASK_COUNT_KEY = "taskCount"
private const val PROJECT_COMPLETE_COUNT_KEY = "completeCount"
private const val PROJECT_DEADLINE_KEY = "deadline"
private const val START_TIME_KEY = "startTime"
private const val TASK_NAME_KEY = "name"
private const val TASK_STATUS_KEY = "status"
private const val TAG = "Repository"

class Repository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser by lazy { MutableLiveData<User>() }
    private val users by lazy { MutableLiveData<List<User>>() }
    private val project by lazy { MutableLiveData<Project>() }
    private val projects by lazy { MutableLiveData<List<Project>>() }
    private val tasks by lazy { MutableLiveData<List<Task>>() }
    private val onGoingTasks by lazy { MutableLiveData<List<Task>>() }
    private lateinit var usersRegistration: ListenerRegistration
    private lateinit var projectsRegistration: ListenerRegistration
    private lateinit var projectRegistration: ListenerRegistration
    private lateinit var tasksRegistration: ListenerRegistration

//    fun getCurrentUserName(): String? {
//        val email = getCurrentUserEmail()
//        val user = email?.let {
//            getUserByEmail(it, {}, {})
//        }
//        return user?.name
//    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

//    fun updateCurrentUserPassword(password: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
//        auth.currentUser?.let {
//            it.updatePassword(password).addOnCompleteListener {
//                    task ->
//                if (task.isSuccessful) {
//                    updateUser(getCurrentUserName()!!, USER_PASSWORD_KEY, password, {}, {})
//                    onSuccessAction()
//                } else {
//                    onFailureAction()
//                }
//            }
//        }
//    }

//    fun updateCurrentUserEmail(email: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
//        auth.currentUser?.let {
//            it.updateEmail(email).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    updateUser(getCurrentUserName()!!, USER_PASSWORD_KEY, email, {}, {})
//                    onSuccessAction()
//                } else {
//                    onFailureAction()
//                }
//            }
//        }
//    }

    fun signOut() {
        auth.signOut()
    }

    fun addUser(user: User, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        Log.d("test1,user's name is ",user.name)
        db.collection(USER_COLLECTION).document(user.name)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID") }
            .addOnFailureListener { e->
                Log.w(TAG, "Error adding document", e) }
    }

    fun updateUser(user: String, key: String, value: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(USER_COLLECTION).document(user)
            .update(key, value)
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun deleteUser(name: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(USER_COLLECTION).document(name)
            .delete()
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun getUserByName(name: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit): MutableLiveData<User> {
        db.collection(USER_COLLECTION).document(name)
            .get()
            .addOnSuccessListener { document ->
                this.currentUser.value = document.toObject(User::class.java)!!
                onSuccessAction()
            }
            .addOnFailureListener { onFailureAction() }
        return this.currentUser
    }

    fun getUserByEmail(email: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit): MutableLiveData<User> {
        db.collection(USER_COLLECTION).whereEqualTo(USER_EMAIL_KEY, email)
            .addSnapshotListener { users, error ->
                if (error != null) {
                    Log.w(TAG, "listen:error", error)
                    onFailureAction()
                    return@addSnapshotListener
                }
                if (users != null && users.size() == 1) {
                    this.currentUser.value = users.iterator().next().toObject()
                    Log.d(TAG, users.iterator().next().toObject(User::class.java).toString())
                    onSuccessAction()

                }
            }
        return this.currentUser
    }

    fun onUsersChange(): LiveData<List<User>> {
        listenForUsersChanges()
        return users
    }

    fun stopListeningForUsersChanges() {
        if (::usersRegistration.isInitialized)
            usersRegistration.remove()
    }

    private fun listenForUsersChanges() {
        usersRegistration = db.collection(USER_COLLECTION).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(TAG, "listen:error", error)
                return@addSnapshotListener
            }

            val users = ArrayList<User>()
            for (user in value!!) {
                users.add(user.toObject())
            }
            this.users.value = users
        }
    }

    fun addProject(project: Project, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(project.name)
            .set(project)
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun updateProject(projectName: String, key: String, value: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(projectName)
            .update(key, value)
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun deleteProject(projectName: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(projectName)
            .delete()
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun incrementProjectCounter(projectName: String, counter: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(projectName)
            .update(counter, FieldValue.increment(1))
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun getProjectByName(projectName: String): MutableLiveData<Project> {
        projectRegistration = db.collection(PROJECT_COLLECTION).document(projectName)
            .addSnapshotListener { project, error ->
                if (error != null) {
                    Log.w(TAG, "listen:error", error)
                    return@addSnapshotListener
                }

                if (project != null) {
                    this.project.value = project.toObject()
                }
            }
        return this.project
    }

    fun stopListeningForProjectChanges() {
        if (::projectRegistration.isInitialized)
            projectRegistration.remove()
    }

    fun onProjectsChange(userName: String, direction: String): LiveData<List<Project>> {
        listenForProjectsChanges(userName, direction)
        return projects
    }

    fun stopListeningForProjectsChanges() {
        if (::projectsRegistration.isInitialized)
            projectsRegistration.remove()
    }

    private fun listenForProjectsChanges(userName: String, direction: String) {
        val direct = when(direction) {
            "ASCENDING" -> Query.Direction.ASCENDING
            "DESCENDING" -> Query.Direction.DESCENDING
            else -> Query.Direction.DESCENDING
        }
        projectsRegistration = db.collection(PROJECT_COLLECTION)
            .whereArrayContains(PROJECT_MEMBERS_KEY, userName)
            .orderBy(PROJECT_DEADLINE_KEY, direct)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "listen:error", error)
                    return@addSnapshotListener
                }

                val projects = ArrayList<Project>()
                for (project in value!!) {
                    projects.add(project.toObject())
                }
                this.projects.value = projects

            }
    }

//    fun getTaskByName(projectName: String, taskName: String, on) {
//
//    }

    fun addTask(task: Task, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(task.project).collection(TASK_COLLECTION)
            .whereEqualTo(TASK_NAME_KEY, task.name).get()
            .addOnSuccessListener { document ->
                if (document != null && document.isEmpty) {
                    val projectRef = db.collection(PROJECT_COLLECTION).document(task.project)

                    projectRef.collection(TASK_COLLECTION).document(task.name)
                        .set(task)
                        .addOnSuccessListener { onSuccessAction() }
                        .addOnFailureListener { onFailureAction() }

                    projectRef.update(PROJECT_MEMBERS_KEY, FieldValue.arrayUnion(task.member))
                        .addOnSuccessListener { onSuccessAction() }
                        .addOnFailureListener { onFailureAction() }

                    incrementProjectCounter(
                        task.project,
                        PROJECT_TASK_COUNT_KEY,
                        onFailureAction,
                        onSuccessAction
                    )
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun updateTask(projectName: String, taskName: String, key: String, value: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(projectName)
            .collection(TASK_COLLECTION).document(taskName)
            .update(key, value)
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun deleteTask(projectName: String, taskName: String, onSuccessAction: () -> Unit, onFailureAction: () -> Unit) {
        db.collection(PROJECT_COLLECTION).document(projectName)
            .collection(TASK_COLLECTION).document(taskName)
            .delete()
            .addOnSuccessListener { onSuccessAction() }
            .addOnFailureListener { onFailureAction() }
    }

    fun getOnGoingTasks(projectName: String): LiveData<List<Task>> {
        db.collection(PROJECT_COLLECTION).document(projectName)
            .collection(TASK_COLLECTION)
            .whereEqualTo(TASK_STATUS_KEY, "on going")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "listen:error", error)
                    return@addSnapshotListener
                }

                val tasks = ArrayList<Task>()
                for (task in value!!) {
                    tasks.add(task.toObject())
                }
                this.onGoingTasks.value = tasks
            }

        return this.onGoingTasks
    }

    fun onTasksChange(projectName: String): MutableLiveData<List<Task>> {
        listenForTasksChanges(projectName)
        return tasks
    }

    fun stopListeningForTasksChangesForProject() {
        if (::tasksRegistration.isInitialized)
            tasksRegistration.remove()
    }

    private fun listenForTasksChanges(projectName: String) {
        tasksRegistration = db.collection(PROJECT_COLLECTION).document(projectName)
            .collection(TASK_COLLECTION)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "listen:error", error)
                    return@addSnapshotListener
                }

                val tasks = ArrayList<Task>()
                for (task in value!!) {
                    tasks.add(task.toObject())
                }
                this.tasks.value = tasks
            }
    }
}