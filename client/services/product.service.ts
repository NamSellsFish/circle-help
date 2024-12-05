import { Product } from "~/types";
import apiSlice from "./api";
import { EXPO_PUBLIC_BASE_URL } from "~/constants";

export const productApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getProducts: builder.query({

            query: ({ }) => {
                return {
                    url: `${EXPO_PUBLIC_BASE_URL}/api/inventory`,
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
                            type: 'Product',
                            id: sku,
                        })),
                        'Product',
                    ]
                    : ['Product'],
        })
    })
});

export const {
    useGetProductsQuery,
} = productApiSlice
