import AsyncStorage from '@react-native-async-storage/async-storage'
import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'
import { persistReducer } from 'redux-persist'
import userReducer from './slices/user.slice'
import cartReducer from './slices/cart.slice'
import filtersReducer from './slices/filters.slice'
import attendanceReducer from './slices/attendance.slice'
import { apiSlice } from '~/services'


const persistConfig = {
    key: 'root',
    version: 1,
    storage: AsyncStorage,
}
const userPersistedReducer = persistReducer(persistConfig, userReducer)



//? Actions
export * from './slices/user.slice'
export * from './slices/cart.slice'
export * from './slices/filters.slice'
export * from './slices/attendance.slice'

export const store = configureStore({
    reducer: {
        user: userPersistedReducer,
        attendance: attendanceReducer,
        cart: cartReducer,
        filters: filtersReducer,
        [apiSlice.reducerPath]: apiSlice.reducer,
    },
    middleware: getDefaultMiddleware =>
        getDefaultMiddleware({
            serializableCheck: false,
        }).concat(apiSlice.middleware),
})

setupListeners(store.dispatch)
