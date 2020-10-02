package events

interface AppEventListener {
    fun appPropertyChanged(propertyName: String, newValue: Any?) {}
}
