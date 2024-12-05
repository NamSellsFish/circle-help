import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: '',
        mode: 'cors',
        prepareHeaders: (headers, { getState }) => {
            const token = getState().user.token
            if (token) headers.set('authorization', token)
            return headers
        },
    }),
    tagTypes: ['Mock Compartment', 'Mock Product', 'User'],
    endpoints: builder => ({}),
})

export default apiSlice