package skip.kit

import skip.lib.*


internal val <E0, E1, E2> Tuple3<E0, E1, E2>.url: E0
    get() = element0

internal val <E0, E1, E2> Tuple3<E0, E1, E2>.filename: E1
    get() = element1

internal val <E0, E1, E2> Tuple3<E0, E1, E2>.mimeType: E2
    get() = element2
