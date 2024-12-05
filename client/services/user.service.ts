import { EXPO_PUBLIC_AUTH_BASE_URL, MOCK_EXPO_PUBLIC_BASE_URL } from "~/constants";
import apiSlice from "./api";

export const userApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        login: builder.mutation({
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_AUTH_BASE_URL}/api/auth/login`,
                method: 'POST',
                headers: { "Authorization": `Basic ${btoa(`${body.user.email}:${body.user.password}`)}` },
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

        editUser: builder.mutation({
            // JSON-server thì dùng id, còn thực tế thì dùng token trên header để xác định user
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_AUTH_BASE_URL}/api/auth/updateProfile`,
                method: 'PATCH',
                body,
            }),
            invalidatesTags: [
                'User',
            ],
        }),

        getUserInfo: builder.query({
            query: () => ({
                url: `${EXPO_PUBLIC_AUTH_BASE_URL}/api/auth/getProfile`,
                method: 'GET',
            }),
            providesTags: ['User'],
        }),
    })
});

export const {
    useLoginMutation,
    useGetUserInfoQuery,
    useEditUserMutation,
} = userApiSlice