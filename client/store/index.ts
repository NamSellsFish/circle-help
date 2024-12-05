import AsyncStorage from '@react-native-async-storage/async-storage'
import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'
import { persistReducer } from 'redux-persist'
import userReducer from './slices/user.slice'
import { apiSlice } from '~/services'


const persistConfig = {
    key: 'root',
    version: 1,
    storage: AsyncStorage,
}

const userPersistedReducer = persistReducer(persistConfig, userReducer)

//? Actions
export * from './slices/user.slice'

export const store = configureStore({
    reducer: {
        user: userPersistedReducer,
        [apiSlice.reducerPath]: apiSlice.reducer,
    },
    middleware: getDefaultMiddleware =>
        getDefaultMiddleware({
            serializableCheck: false,
        }).concat(apiSlice.middleware),
})

setupListeners(store.dispatch)
