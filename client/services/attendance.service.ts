import { EXPO_PUBLIC_BASE_URL } from "~/constants";
import apiSlice from "./api";

export const attendanceApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getAttendanceInfoItems: builder.query({
            query: () => {
                return {
                    url: `${EXPO_PUBLIC_BASE_URL}/api/attendanceInfoItems`,
                    method: 'GET',
                }
            },
            forceRefetch: ({ currentArg, previousArg }) => true,
            merge: (currentCache, newItems) => {
                return [...newItems]
            },
        }),
        checkAttendance: builder.mutation({
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_BASE_URL}/api/checkAttendance`,
                method: 'POST',
                body
            }),
        }),
    })
});

export const {
    useGetAttendanceInfoItemsQuery,
    useCheckAttendanceMutation,
    endpoints
} = attendanceApiSlice