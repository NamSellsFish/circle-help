import apiSlice from './api'

export const userApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        login: builder.mutation({
            query: ({ body }) => ({
                url: '/api/users/login',
                method: 'POST',
                body,
            }),
            invalidatesTags: [
                'User'
            ],
        }),

        getUserInfo: builder.query({
            query: () => ({
                url: '/api/user',
                method: 'GET',
            }),
            providesTags: ['User'],
        }),
    }),
})

export const {
    useLoginMutation,
    useGetUserInfoQuery,
} = userApiSlice
