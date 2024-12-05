import { AntDesign, FontAwesome } from "@expo/vector-icons";
import { router, Stack, useLocalSearchParams } from "expo-router";
import React, { useEffect, useState } from "react";
import { Pressable, ScrollView, Text, View } from 'react-native'
import Tooltip from 'react-native-walkthrough-tooltip';
import { AuthWrapper, CommandBar, CompartmentModal, Filter, ProductTable, Sort } from "~/components";
import { Tab } from '@rneui/themed';
import { useAppDispatch, useAppSelector, useDidMountEffect, useDisclosure } from "~/hooks";
import { useGetCompartmentsQuery, useFillAMockCompartmentMutation, useGetProductsQuery, useGetMockProductsQuery } from "~/services";
import { Gesture, GestureDetector } from "react-native-gesture-handler";
import { CompartmentPosition } from "~/types";
import { addToCart, removeFromCart, switchStatus } from "~/store";
import Toast from "react-native-toast-message";

function ShelfAndInventoryScreen() {
    const params = useLocalSearchParams()
    const sortCol = params.sortCol
    const sortOption = params?.sortOption
    const minPrice = params?.minPrice
    const maxPrice = params?.maxPrice
    const minQuantity = params?.minQuantity
    const maxQuantity = params?.maxQuantity
    const page = params?.page || 0
    const page_size = params?.page_size
    const [isShowWarningTooltip, warningTooltipHandlers] = useDisclosure();
    const [isShowCompartmentModal, compartmentModalHandlers] = useDisclosure();
    const [rowNo, setRowNo] = useState(0);
    // const { data, isSuccess, refetch } = useGetMockCompartmentsQuery({ row: rowNo + 1 });
    // const { data: productsData, isSuccess: isProductSuccess } = useGetMockProductsQuery({ sortCol, sortOption, minPrice, maxPrice, maxQuantity, minQuantity });
    const { data, isSuccess, refetch, isError, error } = useGetCompartmentsQuery({ row: rowNo + 1 });
    const { data: productsData, isSuccess: isProductSuccess, isFetching, isLoading } = useGetProductsQuery({ sortCol, sortOption, minPrice, maxPrice, maxQuantity, minQuantity, page, page_size });
    const dispatch = useAppDispatch()
    //? Store
    // @ts-ignore
    const { items: selectedItems, status, isZigZagMode, foundItems } = useAppSelector(state => state.cart)
    const { token } = useAppSelector(state => state.user)



    //? Handlers
    const handleShelfLevelTabChange = (value: number) => {
        setRowNo(value);
    }

    const singleTapCompartmentGesture = (compartmentNo: number) => {
        return Gesture.Tap()
            // @ts-ignore
            .onEnd(() => compartmentModalHandlers.open(compartmentNo))
            .runOnJS(true);
    };

    const handleCompartmentLongPress = (item: CompartmentPosition) => {
        const isInCart = selectedItems['src'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(item)) !== -1 || selectedItems['des'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(item)) !== -1

        if (!isInCart) {
            dispatch(switchStatus())
            dispatch(addToCart(item))
        }
    }

    const doubleTapCompartmentGesture = (item: CompartmentPosition) => {
        return Gesture.Tap()
            .numberOfTaps(2)
            .onEnd(() => {
                const isInCart = selectedItems['src'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(item)) !== -1 || selectedItems['des'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(item)) !== -1

                if (isInCart) {
                    dispatch(removeFromCart(item))

                } else {
                    if (isZigZagMode) dispatch(switchStatus())
                    dispatch(addToCart(item))
                }
            })
            .runOnJS(true)
    }

    const tripleTapCompartmentGesture = (item: CompartmentPosition) => {
        return Gesture.Tap()
            .numberOfTaps(3)
            .onEnd(() => {
                const isInCart = selectedItems['src'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(item)) !== -1 || selectedItems['des'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(item)) !== -1
                //@ts-ignore
                const found = data.find((compartment) => compartment.position.compartmentNo === item.compartmentNo).productDetails
                if (isInCart) {
                    dispatch(removeFromCart(item))

                } else {
                    if (found === null) {
                        Toast.show({
                            type: 'error',
                            text2: 'This compartment did not contain any product to move to inventory',
                        })
                        return
                    }
                    if (status === 'des') dispatch(switchStatus())
                    dispatch(addToCart(item))
                    dispatch(switchStatus())
                    dispatch(addToCart(null))
                    dispatch(switchStatus())
                }
            })
            .runOnJS(true)
    }

    useDidMountEffect(() => {
        Toast.show({
            type: 'info',
            text2: `${isZigZagMode ? 'ZigZag selection' : 'Sequential selection'} is activated`,
        })
    }, [isZigZagMode]);

    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: true,
                    title: '',
                    headerStyle: {
                        // @ts-ignore
                        height: 50,
                    },
                    header: props => token !== '' && <CommandBar row={rowNo + 1} productsData={productsData} compartmentsData={data} />,

                }}
            />
            <AuthWrapper>
                <View className="container h-fit shrink-0 mx-auto px-5 py-2 lg:px-32 lg:pt-12 align-middle">
                    {/* {isFetching === true && <Text className="text-red-500">{JSON.stringify(error)}k</Text>} */}
                    {/* Shelf-level Tab list */}
                    <View className="container order-first flex-grow-0 h-15 mx-auto mb-2 px-1 py-0" >
                        <Tab
                            dense
                            value={rowNo}
                            onChange={handleShelfLevelTabChange}
                            indicatorStyle={{
                                backgroundColor: 'white',
                                height: 3,
                            }}
                            buttonStyle={{
                                backgroundColor: 'black',
                                height: 40,
                            }}
                            variant="primary"
                        >
                            <Tab.Item
                                title="Row 1"
                                titleStyle={{ fontSize: 7 }}
                                icon={{ name: 'layers-outline', type: 'ionicon', color: 'white' }}
                            />
                            <Tab.Item
                                title="Row 2"
                                titleStyle={{ fontSize: 7 }}
                                icon={{ name: 'layers-outline', type: 'ionicon', color: 'white' }}
                            />
                            <Tab.Item
                                title="Row 3"
                                titleStyle={{ fontSize: 7 }}
                                icon={{ name: 'layers-outline', type: 'ionicon', color: 'white' }}
                            />
                        </Tab>

                    </View>
                    {/* Compartment Gallery */}
                    <View className={`ml-4 pt-1 h-80 w-[90%] flex flex-row flex-wrap border-dashed border-2 `}>
                        {isSuccess &&
                            Array.from({ length: 100 }, (_, i) => i).map((_, i) => {
                                let compartment = data.find((compartment: { position: { compartmentNo: number } }) => compartment.position.compartmentNo === i)
                                let compartmentStatus = compartment?.statusCode;
                                let compartmentPosition = compartment?.position as CompartmentPosition;
                                let compartmentPositionInCartSrc = selectedItems['src'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(compartmentPosition))
                                let compartmentPositionInCartDes = selectedItems['des'].findIndex((i: CompartmentPosition) => JSON.stringify(i) === JSON.stringify(compartmentPosition))
                                if ([3, 4, 5, 6, 7, 13, 17, 23, 27, 33, 34, 35, 36, 37, 38, 39, 43, 44, 45, 46, 47, 48, 49, 53, 54, 55, 56, 57, 58, 59, 63, 67, 73, 77, 83, 87, 93, 94, 95, 96, 97].includes(i)) return (<Pressable disabled={true} onPress={() => { }} key={i} className={`flex w-[8%] h-[8%] ml-1 mb-1 rounded-lg`} />)
                                else return (

                                    <Pressable
                                        onLongPress={() => handleCompartmentLongPress(compartmentPosition)}
                                        key={i} className={`flex w-[8%] h-[8%] border-solid border-4 
                                     ${(// @ts-ignore
                                                foundItems.findIndex(item => JSON.stringify(item).includes(JSON.stringify(compartmentPosition))) !== -1 && 'border-blue-400') || (compartmentPositionInCartSrc !== -1 && selectedItems.des[compartmentPositionInCartSrc] === null ? 'border-purple-500' : compartmentPositionInCartSrc !== -1 ? 'border-red-500' : compartmentPositionInCartDes !== -1 ? 'border-green-500' : 'border-black')}
                                        ml-1 mb-1 rounded-lg 
                                     ${compartment['productDetails'] !== null ? 'bg-gray-400' : 'bg-[url(https://st2.depositphotos.com/2009363/8591/v/950/depositphotos_85916628-stock-illustration-the-gray-and-white-squares.jpg)] bg-cover'}`}>
                                        <GestureDetector gesture={Gesture.Exclusive(tripleTapCompartmentGesture(compartmentPosition), doubleTapCompartmentGesture(compartmentPosition), singleTapCompartmentGesture(i))}>
                                            <View className="block z-[0] w-full h-full">
                                                <Text data-twe-toggle="tooltip" className="flex items-center text-[8px] h-full w-full absolute left-[0.5px]">
                                                    {compartmentPositionInCartSrc !== -1 && selectedItems.des[compartmentPositionInCartSrc] === null ? compartment.position.compartmentNoFromUserPerspective : compartmentPositionInCartSrc !== -1 ? compartmentPositionInCartSrc : compartmentPositionInCartDes !== -1 ? compartmentPositionInCartDes : compartment.position.compartmentNoFromUserPerspective}
                                                </Text>

                                                <CompartmentModal isShow={isShowCompartmentModal === i}
                                                    // @ts-ignore
                                                    onClose={compartmentModalHandlers.close} data={compartment} />
                                            </View>
                                        </GestureDetector>
                                        <Tooltip
                                            arrowSize={{ width: 16, height: 10 }}
                                            isVisible={isShowWarningTooltip === i}
                                            disableShadow
                                            content={
                                                <>

                                                    {<Text>{compartmentStatus === 2 ? "📅 1 day before the date → Needs to be taken down" : compartmentStatus === 3 ? "💲1 week before the date → Needs to be displayed in the front compartments, at eye level with a discount or 'buy 2 get 1 free' promo" : compartmentStatus === 4 ? "🔖 Event time (e.g., The Red Festival) → Needs to be gathered into event shelf" : '  '}</Text>}
                                                </>
                                            }
                                            // @ts-ignore
                                            onClose={warningTooltipHandlers.close}
                                            placement="bottom"

                                        >
                                            {(compartmentStatus === 3 || compartmentStatus === 4 || compartmentStatus === 2) && <FontAwesome
                                                // @ts-ignore
                                                onPress={() => warningTooltipHandlers.open(compartment.position.compartmentNo)} name="warning" size={12} color='yellow' className="z-[10] text-yellow-400 absolute -top-5 -right-1" />}
                                        </Tooltip>
                                    </Pressable>

                                )
                            }
                            )
                        }
                    </View>
                    {/* Inventory  */}
                    <View className="px-1 mt-3 h-[37vh]">
                        {/* Filters & Sort */}
                        <View className="divide-y-2 divide-neutral-200 mx-3 -mb-5">
                            <View className="flex flex-row justify-between align-baseline py-2 gap-x-3">
                                {<><Filter />
                                    <Text onPress={() => { router.navigate('/productImporter') }} className="text-base">
                                        <AntDesign name="barcode" size={12} color="black" />
                                        <Text> Import product(s)</Text>
                                    </Text>
                                    <Sort />

                                </>
                                }
                            </View>

                            <View className="flex flex-row justify-between py-2">
                                <Text className="text-base text-neutral-600">All product(s) available</Text>

                                <Text className="text-base text-neutral-600">
                                    {productsData?.content?.length} item(s)
                                </Text>
                            </View>
                        </View>
                        {isProductSuccess && <ProductTable productsData={productsData.content} page={productsData.page} />}
                    </View>
                </View >
            </AuthWrapper>
        </>
    );
}

export default ShelfAndInventoryScreen;