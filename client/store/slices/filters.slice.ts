import { PayloadAction, createSlice } from '@reduxjs/toolkit'

export interface FilterState {
    isKeepFilterModalOpen: boolean
}

const initialState: FilterState = {
    isKeepFilterModalOpen: false,
}

const filterSlice = createSlice({
    name: 'filter',
    initialState,
    reducers: {
        setKeepFilterModalOpen(state, action: PayloadAction<boolean>) {
            state.isKeepFilterModalOpen = action.payload
        },
    },
})

export const { setKeepFilterModalOpen } = filterSlice.actions

export default filterSlice.reducer
