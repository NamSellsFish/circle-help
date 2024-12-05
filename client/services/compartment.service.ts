import { EXPO_PUBLIC_BASE_URL } from "~/constants";
import apiSlice from "./api";

export const compartmentApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getCompartments: builder.query({
            query: ({ row }) => {
                return {
                    url: `${EXPO_PUBLIC_BASE_URL}/api/shelves/get?row=${row}`,
                    method: 'GET',
                }
            },
            forceRefetch: ({ currentArg, previousArg }) => true,
            merge: (currentCache, newItems) => {
                return [...newItems]
            },
        }),
        stockShelvesManually: builder.mutation({
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_BASE_URL}/api/shelves/manualMove`,
                method: 'POST',
                body
            }),
        }),
        stockShelvesAutomatically: builder.mutation({
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_BASE_URL}/api/shelves/autoMove`,
                method: 'POST',
                params: {
                    slowSellCheck: body.slowSellCheck,
                    event: body.event,
                }
            }),

        }),

    })
});

export const {
    useGetCompartmentsQuery,
    useStockShelvesAutomaticallyMutation,
    useStockShelvesManuallyMutation
} = compartmentApiSlice