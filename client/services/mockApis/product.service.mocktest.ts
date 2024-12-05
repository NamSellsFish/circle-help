import { Product } from "~/types";
import apiSlice from "../api";
import { MOCK_EXPO_PUBLIC_BASE_URL } from "~/constants";

export const mockProductApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getMockProducts: builder.query({

            query: ({ sortCol, sortOption, minPrice, maxPrice, maxQuantity, minQuantity }) => {
                const params = {
                    _sort: sortCol,
                    _order: sortOption,
                    price_gte: minPrice,
                    price_lte: maxPrice,
                    quantity_gte: minQuantity,
                    quantity_lte: maxQuantity
                };

                const filteredParams = Object.fromEntries(
                    Object.entries(params).filter(([key, value]) => value !== '')
                );
                return {
                    url: `${MOCK_EXPO_PUBLIC_BASE_URL}/inventory`,
                    method: 'GET',
                    params: filteredParams
                }
            },
            serializeQueryArgs: ({ queryArgs, ...rest }) => {
                const newQueryArgs = { ...queryArgs }
                if (newQueryArgs.page) {
                    delete newQueryArgs.page
                }
                return newQueryArgs
            },
            merge: (currentCache, newItems) => {
                return [...newItems]
            },
        })
    })
});

export const {
    useGetMockProductsQuery,
} = mockProductApiSlice
