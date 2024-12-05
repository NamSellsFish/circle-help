import { MOCK_EXPO_PUBLIC_BASE_URL } from "~/constants";
import apiSlice from "../api";



export const mockCompartmentApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getMockCompartments: builder.query({
            query: ({ row }) => {
                return {
                    url: `${MOCK_EXPO_PUBLIC_BASE_URL}/itemOnCompartments?position.rowNo=${row}`,
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
                                type: 'Mock Compartment',
                                id: position.compartmentNo,
                            };
                            return tag;
                        }),
                        'Mock Compartment',
                    ]
                    : ['Mock Compartment']
        }),
        fillAMockCompartment: builder.mutation({
            query: ({ body }) => ({
                url: `${MOCK_EXPO_PUBLIC_BASE_URL}/itemOnCompartments/${body.id}`,
                method: 'PUT',
                body,
            }),
            invalidatesTags: (result, error, arg) => {
                return [{ type: 'Mock Compartment', id: arg.id }]
            },

        }),
    })
});

export const {
    useGetMockCompartmentsQuery,
    useFillAMockCompartmentMutation,
} = mockCompartmentApiSlice