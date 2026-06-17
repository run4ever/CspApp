package fr.csp.app.util

import dev.gitlive.firebase.storage.Data

actual fun ByteArray.toStorageData(): Data = Data(this)
