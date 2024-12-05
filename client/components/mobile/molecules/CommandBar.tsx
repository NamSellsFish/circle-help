import { PropsWithChildren, useRef, useState } from "react";
import { SearchBar } from '@rneui/themed';
import { TouchableWithoutFeedback, View } from "react-native";
import { FontAwesome } from "@expo/vector-icons";
import { useAppDispatch, useAppSelector, useChangeRoute, useDisclosure } from "~/hooks";
import Button from "../atoms/Button";
import ResponsiveImage from "../atoms/ResponsiveImage";
import { HandleResponse, ShelvesStockingConfirmModal } from "~/components";
import { switchMode, resetCartState, addFoundItems } from "~/store";
import React from "react";
import { useGetCompartmentsQuery, useGetProductsQuery, useStockShelvesManuallyMutation } from "~/services";
import { ref } from "yup";

type CommandBarProps = PropsWithChildren<{ row?: number, productsData: object[], compartmentsData: [] }>

export default function CommandBar({ row, productsData, compartmentsData }: CommandBarProps) {
    const [searchTxt, setSearchTxt] = useState('');
    const [isShowShelvesStockingConfirmModal, shelvesStockingConfirmModalHandlers] = useDisclosure();
    const [isOpenSearchBar, searchBarToogleHandlers] = useDisclosure()
    // @ts-ignore
    const { items: selectedItems } = useAppSelector(state => state.cart)
    const [stockShelvesManually, { isSuccess, isError }] = useStockShelvesManuallyMutation()
    const { refetch: refetchCompartments } = useGetCompartmentsQuery({ row: row });
    const { refetch: refetchProducts } = useGetProductsQuery({ page: 0 })
    const searchBarRef = useRef(null);
    const dispatch = useAppDispatch()
    const changeRoute = useChangeRoute()


    const handleSearchIconPress = () => {
        // @ts-ignore
        searchBarToogleHandlers.open();
        if (searchBarRef.current) {
            // @ts-ignore
            searchBarRef.current.focus();
        }
    };

    const handleSearchBarClose = () => {
        searchBarToogleHandlers.close();
    }

    const handleSwitchMode = () => {
        dispatch(switchMode());
    }
    const handleSubmitShelvesStockingOptions = () => {
        stockShelvesManually(
            {
                body: selectedItems
            }
        )
    }

    //@ts-ignore
    const handleOnChange = (searchTxt) => {
        setSearchTxt(searchTxt)
        //@ts-ignore
        const foundItemsInProducts = productsData.content.filter((product: { sku: string, packageID: number }) => searchTxt !== '' && (product.sku.includes(searchTxt) || product.packageID === parseInt(searchTxt)))
        //@ts-ignore
        const foundItemsInCompartments = compartmentsData.filter((compartment) => searchTxt !== '' && (compartment.productDetails?.sku === searchTxt || compartment.productDetails?.packageID === parseInt(searchTxt)))
        dispatch(addFoundItems({ foundItems: [...foundItemsInCompartments, ...foundItemsInProducts] }))
    }

    const onSuccess = () => {
        refetchCompartments()
        refetchProducts()
        changeRoute({})
        dispatch(resetCartState())
    }

    return (
        <>

            {/*  Handle Login Response */}
            {(isSuccess || isError) && (
                <HandleResponse
                    isError={isError}
                    isSuccess={isSuccess}
                    // @ts-ignore data.data?.errors?.body[0]
                    error={'Abnormal!'}
                    message={`Stocking shelves successfully`}
                    onSuccess={onSuccess}
                />
            )}
            <View style={{ flexDirection: "column", justifyContent: 'flex-start', gap: 5, flexWrap: 'wrap', marginTop: 30, marginBottom: 10 }} >
                <View style={{ flexDirection: "row", justifyContent: 'space-between', alignItems: 'center', gap: 2, flexWrap: 'wrap', height: 50 }} >
                    {/* <FontAwesome name="refresh" size={32} color="black" style={{ height: isOpenSearchBar === -999 ? 32 : 0, marginLeft: 5, opacity: isOpenSearchBar === -999 ? 1 : 0, }} /> */}
                    <ResponsiveImage alt={"Circle K"} style={{ width: '20%', opacity: isOpenSearchBar === -999 ? 1 : 0, height: isOpenSearchBar === -999 ? '80%' : 0 }} source={'https://credentials.goldena.vn/wp-content/uploads/2023/06/Circle-K-Symbol.png'} />
                    {/* @ts-ignore */}
                    <SearchBar ref={searchBarRef} onChangeText={handleOnChange} onBlur={() => searchBarToogleHandlers.close()} placeholder="Type exactly your sku or packageID" value={searchTxt} lightTheme={true} inputContainerStyle={{ height: 20 }} inputStyle={{ height: 20, minHeight: 4 }} containerStyle={{ width: isOpenSearchBar === -999 ? 45 : '100%', borderRadius: 10, alignSelf: 'flex-end', height: '100%' }} searchIcon={<FontAwesome name="search" size={20} color="black" onPress={() => { searchBarToogleHandlers.open(); this.current?.focus() }} />}></SearchBar>
                </View>
                <View style={{ flexDirection: "row", justifyContent: 'center', gap: 5, flexWrap: 'wrap' }} >
                    {/* @ts-ignore */}
                    <Button onPress={() => shelvesStockingConfirmModalHandlers.open()} style={{ backgroundColor: 'gainsboro', alignItems: 'center', paddingHorizontal: 5, paddingVertical: 10 }}>STOCKING SHELVES (Auto)</Button>
                    {/* @ts-ignore */}
                    {selectedItems.src.filter(i => typeof i !== 'undefined').length === selectedItems.des.filter(i => typeof i !== 'undefined').length && (selectedItems.src.length + selectedItems.des.length) > 0 && <Button onPress={handleSubmitShelvesStockingOptions} style={{ backgroundColor: 'gainsboro', alignItems: 'center', paddingHorizontal: 5, paddingVertical: 10 }}>STOCKING SHELVES (Manually)</Button>}
                    {/* @ts-ignore */}
                    {(selectedItems.src.filter(i => typeof i !== 'undefined').length !== 0 || selectedItems.des.filter(i => typeof i !== 'undefined').length !== 0)
                        &&
                        <>
                            <Button onPress={() => dispatch(resetCartState())} style={{ backgroundColor: 'gainsboro', alignItems: 'center', paddingHorizontal: 5, paddingVertical: 10 }}>CLEAR</Button>

                            <Button onPress={handleSwitchMode} style={{ backgroundColor: 'gainsboro', alignItems: 'center', paddingHorizontal: 5, paddingVertical: 10 }}>SWITCH MODE</Button>
                        </>
                    }
                </View>
            </View >
            <ShelvesStockingConfirmModal row={row} isShow={isShowShelvesStockingConfirmModal === -111}
                // @ts-ignore
                onClose={shelvesStockingConfirmModalHandlers.close} modalHeaderLabel={"Automatic stocking shelves"} />
        </>
    )
}