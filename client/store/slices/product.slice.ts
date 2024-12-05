import { createSlice } from "@reduxjs/toolkit";
import { array } from "yup";

export interface productSpec {
    sku: string,
    name: string,
    price: string,
    wholesalePrice: string,
    quantity: string,
    imageUrl: string,
    categories: string,
}

const initialState: productSpec = {
    sku: '',
    name: '',
    price: '',
    wholesalePrice: '',
    quantity: '',
    imageUrl: '',
    categories: '',
}

export interface importProductForm {
    packageID: number
	supplier: string
	sku: string
	name: string
	price: number
	wholesalePrice: number
	quantity: number
	expirationDate: string
	imageUrl: string
    categories: string
	note: string
}

const productSlice = createSlice({
    name: 'product',
    initialState,
    reducers: {
        updateProductInfoItem: (state, action) => {
            Object.assign(state, action.payload)
        },
        resetProductInfoItem: (state) => {
            Object.assign(state, initialState)
        },
    }
})

export const {
    updateProductInfoItem,
    resetProductInfoItem
} = productSlice.actions

export default productSlice.reducer