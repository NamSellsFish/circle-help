import { EXPO_PUBLIC_AUTH_BASE_URL } from "~/constants";
import apiSlice from "./api";

export const userApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        login: builder.mutation({
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_AUTH_BASE_URL}/api/auth/login`,
                method: 'POST',
                headers: { "Authorization": `Basic ${btoa(`${body.user.email}:${body.user.password}`)}` }
            }),
            transformResponse: (response, meta) => {
                // Capture headers from the response
                const headers = meta?.response?.headers;
                return { data: response, headers };
            },
            invalidatesTags: [
                'User',
            ],
        }),

        getUserInfo: builder.query({
            query: () => ({
                url: `${EXPO_PUBLIC_AUTH_BASE_URL}/api/auth/user`,
                method: 'GET',
            }),
            providesTags: ['User'],
        }),
    })
});

export const {
    useLoginMutation,
    useGetUserInfoQuery
} = userApiSlice