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
            forceRefetch({ currentArg, previousArg }) {
                if (currentArg?.row === 1) return false
                return currentArg?.row !== previousArg?.row
            },
            providesTags: result =>
                result
                    ? [
                        // @ts-ignore
                        ...result.map(({ position }) => {
                            const tag = {
                                type: 'Compartment',
                                id: position.compartmentNo,
                            };
                            return tag;
                        }),
                        'Compartment',
                    ]
                    : ['Compartment']
        })
    })
});

export const {
    useGetCompartmentsQuery
} = compartmentApiSlice