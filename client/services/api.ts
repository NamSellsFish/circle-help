import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: '',
        mode: 'cors',
        prepareHeaders: (headers, { getState }) => {

        },
    }),
    tagTypes: ['Mock Compartment', 'Mock Product', 'Compartment', 'Product', 'User'],
    endpoints: builder => ({}),
})

export default apiSlice