import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: process.env.EXPO_PUBLIC_BASE_URL,
        prepareHeaders: (headers, { getState }) => {
            // @ts-ignore
            const token = getState().user.token
            if (token) headers.set('authorization', `Token ${token}`)
            return headers
        },
    }),
    tagTypes: ['User'],
    endpoints: builder => ({}),
})

export default apiSlice