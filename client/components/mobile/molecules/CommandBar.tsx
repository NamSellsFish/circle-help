import { PropsWithChildren, useRef } from "react";
import { SearchBar } from '@rneui/themed';
import { View } from "react-native";
import { FontAwesome } from "@expo/vector-icons";
import { useDisclosure } from "~/hooks";
import Button from "../atoms/Button";
import ResponsiveImage from "../atoms/ResponsiveImage";
import { ShelvesStockingConfirmModal } from "~/components";
import React from "react";

type CommandBarProps = PropsWithChildren<{}>

export default function CommandBar({ }: CommandBarProps) {
    const [isShowShelvesStockingConfirmModal, shelvesStockingConfirmModalHandlers] = useDisclosure();
    const [isOpenSearchBar, searchBarToogleHandlers] = useDisclosure()
    const searchBarRef = useRef(null);
    const handleSearchIconPress = () => {
        // @ts-ignore
        searchBarToogleHandlers.open();
        if (searchBarRef.current) {
            // @ts-ignore
            searchBarRef.current.focus();
        }
    };

    return (
        <>
            <View style={{ flexDirection: "column", justifyContent: 'flex-start', gap: 2, flexWrap: 'wrap' }} >
                <View style={{ flexDirection: "row", justifyContent: 'space-between', alignItems: 'center', gap: 2, flexWrap: 'wrap' }} >
                    {/* <FontAwesome name="refresh" size={32} color="black" style={{ height: isOpenSearchBar === -999 ? 32 : 0, marginLeft: 5, opacity: isOpenSearchBar === -999 ? 1 : 0, }} /> */}
                    <ResponsiveImage alt={"Circle K"} style={{ width: '20%', opacity: isOpenSearchBar === -999 ? 1 : 0, height: isOpenSearchBar === -999 ? '78%' : 0 }} source={'https://credentials.goldena.vn/wp-content/uploads/2023/06/Circle-K-Symbol.png'} />
                    {/* @ts-ignore */}
                    <SearchBar ref={searchBarRef} onBlur={searchBarToogleHandlers.close} lightTheme={true} inputContainerStyle={{ height: 20 }} inputStyle={{ height: 0, minHeight: 16 }} containerStyle={{ width: isOpenSearchBar === -999 ? 45 : '100%', borderRadius: 10, justifySelf: 'flex-end' }} searchIcon={<FontAwesome name="search" size={16} color="black" onPress={handleSearchIconPress} />}></SearchBar>

                </View>
                <View style={{ flexDirection: "row", justifyContent: 'center', gap: 5, flexWrap: 'wrap' }} >
                    {/* @ts-ignore */}
                    <Button style={{ backgroundColor: 'gainsboro' }} moreTextClassNames="rounded  px-3 pb-2 pt-2.5 text-xs font-medium uppercase leading-normal text-neutral-600 shadow-light-3 transition duration-150 ease-in-out hover:bg-neutral-200 hover:shadow-light-2 focus:bg-neutral-200 focus:shadow-light-2 focus:outline-none focus:ring-0 active:bg-neutral-200 active:shadow-light-2 motion-reduce:transition-none dark:shadow-black/30 dark:hover:shadow-dark-strong dark:focus:shadow-dark-strong dark:active:shadow-dark-strong" onPress={() => shelvesStockingConfirmModalHandlers.open()} isLoading={false}>STOCKING SHELVES (Auto)</Button>
                </View>
            </View>
            <ShelvesStockingConfirmModal isShow={isShowShelvesStockingConfirmModal === -111}
                // @ts-ignore
                onClose={shelvesStockingConfirmModalHandlers.close} modalHeaderLabel={"Automatic stocking shelves"} />
        </>
    )
}