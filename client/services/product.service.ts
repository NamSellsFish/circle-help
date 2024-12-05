import { Product } from "~/types";
import apiSlice from "./api";
import { EXPO_PUBLIC_BASE_URL } from "~/constants";

export const productApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getProducts: builder.query({
            query: ({ sortCol, sortOption, minPrice, maxPrice, maxQuantity, minQuantity, page, page_size }) => {
                const params = {
                    sortColumn: sortCol,
                    sortOption,
                    minPrice,
                    maxPrice,
                    minQuantity,
                    maxQuantity,
                    page,
                    page_size,
                };

                const filteredParams = Object.fromEntries(
                    Object.entries(params).filter(([key, value]) => value !== '')
                );

                return {
                    url: `${EXPO_PUBLIC_BASE_URL}/api/inventory`,
                    method: 'GET',
                    params: filteredParams
                }
            },
            forceRefetch: ({ currentArg, previousArg }) => true,
            // merge: (currentCache, newItems) => {
            //     return [...newItems.content]
            // },
        }),
        productSpec: builder.query({
            query: ({ sku }) => {
                return {
                    url: `${EXPO_PUBLIC_BASE_URL}/api/inventory/productSpec?sku=${sku}`,
                    method: 'GET',
                }
            }
        }),
        importProduct: builder.mutation({
            query: ({ body }) => ({
                url: `${EXPO_PUBLIC_BASE_URL}/api/inventory/importProduct`,
                method: 'POST',
                body: body
            }),
        })
    })
});

export const {
    useGetProductsQuery,
    useProductSpecQuery,
    useImportProductMutation
} = productApiSlice

const originalUseImportProductMutation = useImportProductMutation;

export const useDebugImportProductMutation = () => {
    const [mutate, result] = originalUseImportProductMutation();

    const debugMutate = async (arg: any) => {
        alert(JSON.stringify(arg));
        return mutate(arg);
    };

    return [debugMutate, result];
};
