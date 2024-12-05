import { Product } from "~/types";
import apiSlice from "../api";
import { MOCK_EXPO_PUBLIC_BASE_URL } from "~/constants";

export const mockProductApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getMockProducts: builder.query({

            query: ({ }) => {
                return {
                    url: `${MOCK_EXPO_PUBLIC_BASE_URL}/inventory`,
                    method: 'GET',
                }
            },
            serializeQueryArgs: ({ queryArgs, ...rest }) => {
                const newQueryArgs = { ...queryArgs }
                if (newQueryArgs.page) {
                    delete newQueryArgs.page
                }
                return newQueryArgs
            },
            // Always merge incoming data to the cache entry
            merge: (currentCache, newItems) => {
                if (currentCache && currentCache !== newItems) {
                    newItems.unshift(...currentCache)
                    return {
                        ...currentCache,
                        ...newItems,
                    }
                }
                return newItems
            },
            // Refetch when the page arg changes
            forceRefetch({ currentArg, previousArg }) {
                if (currentArg?.page === 1) return false
                return currentArg?.page !== previousArg?.page
            },
            providesTags: result =>
                result
                    ? [
                        ...result.map(({ sku }: Product) => ({
                            type: 'Mock Product',
                            id: sku,
                        })),
                        'Mock Product',
                    ]
                    : ['Mock Product'],
        })
    })
});

export const {
    useGetMockProductsQuery,
} = mockProductApiSlice
