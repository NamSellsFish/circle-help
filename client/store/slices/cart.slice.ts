import { createSlice } from "@reduxjs/toolkit";
import { CompartmentPosition } from "~/types";

// Cái cart không phải chỉ dùng khi khách mua hàng
export interface cartState {
    items: { "src": (CompartmentPosition | { sku: string, packageID: number } | undefined)[]; "des": (CompartmentPosition | null | undefined)[]; };
    foundItems: (CompartmentPosition | { sku: string, packageID: number } | undefined)[];
    status: 'src' | 'des';
    isZigZagMode: boolean;
}

const initialState: cartState = {
    items: { "src": [], "des": [] },
    foundItems: [],
    status: 'src',
    isZigZagMode: false
};

const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {
        addFoundItems: (state, action) => {
            const { foundItems } = action.payload
            state.foundItems = [...foundItems]
        },
        switchStatus: (state) => {
            state.status = state.status === 'src' ? 'des' : 'src'
        },
        switchMode: (state) => {
            state.isZigZagMode = !state.isZigZagMode
        },
        addToCart: (state, action) => {
            let firstUndefinedIndex = state.items[state.status].findIndex(item => item === undefined)
            if (firstUndefinedIndex !== -1) { state.items[state.status][firstUndefinedIndex] = action.payload }
            else {
                state.items[state.status].push(action.payload)
            }
        },
        removeFromCart: (state, action) => {
            const indexInSrc = state.items['src'].findIndex(item => JSON.stringify(item) === JSON.stringify(action.payload))
            const indexInDes = state.items['des'].findIndex(item => JSON.stringify(item) === JSON.stringify(action.payload))


            if (indexInSrc !== -1) {
                if (state.items['des'][indexInSrc] !== undefined && state.items['des'][indexInSrc] !== null) { delete state.items['src'][indexInSrc] }
                else { state.items['src'].splice(indexInSrc, 1); state.items['des'].splice(indexInSrc, 1) }

            }
            if (indexInDes !== -1) {
                if (state.items['src'][indexInDes] !== undefined) { delete state.items['des'][indexInDes] }
                else { state.items['src'].splice(indexInDes, 1); state.items['des'].splice(indexInDes, 1) }
            }
        },
        resetCartState: () => initialState,
    }
})

export const {
    switchStatus,
    switchMode,
    addToCart,
    addFoundItems,
    removeFromCart,
    resetCartState
} = cartSlice.actions

export default cartSlice.reducer