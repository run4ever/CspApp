package fr.csp.app.util

import dev.gitlive.firebase.storage.Data

expect fun ByteArray.toStorageData(): Data
