import { createSlice } from '@reduxjs/toolkit'

type userSliceState = { token: string }

const initialState: userSliceState = {
    token: '',
}

const userSlice = createSlice({
    name: 'user',
    initialState,
    reducers: {

        userLogout: state => {
            state.token = ''
        },

        userLogIn: (state, action) => {
            state.token = action.payload
        },
    },
})

export const { userLogIn, userLogout } = userSlice.actions

export default userSlice.reducer
