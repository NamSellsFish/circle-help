import '../global.css';
import { Stack } from 'expo-router';
import { SafeAreaProvider } from 'react-native-safe-area-context'
import Toast from 'react-native-toast-message'
import { Provider } from 'react-redux'
import { persistStore } from 'redux-persist'
import { PersistGate } from 'redux-persist/integration/react'
import { store } from '~/store'

const persistor = persistStore(store)

export default function RootLayout() {
  return (<Provider store={store}>
    <PersistGate loading={null} persistor={persistor}>
      <SafeAreaProvider>
        <Stack>
          <Stack.Screen
            name="(main)/(tabs)"
            options={{
              headerShown: false,
            }}
          />
        </Stack>
        <Toast />
      </SafeAreaProvider>
    </PersistGate>

  </Provider>
  )
}
