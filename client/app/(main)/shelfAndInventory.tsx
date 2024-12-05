import { FontAwesome } from "@expo/vector-icons";
import { Stack } from "expo-router";
import React, { useEffect, useState } from "react";
import { Pressable, ScrollView, Text, View } from 'react-native'
import Tooltip from 'react-native-walkthrough-tooltip';
import { CommandBar, CompartmentModal, ProductTable } from "~/components";
import { Tab } from '@rneui/themed';
import { ListItem } from '@rneui/themed';
import JSONTree from "react-native-json-tree";
import { MOCK_STOCKING_PAIRS } from "~/constants";
import { useDisclosure } from "~/hooks";
import { useGetMockCompartmentsQuery, useGetCompartmentsQuery, useGetMockProductsQuery, useFillAMockCompartmentMutation, useGetProductsQuery } from "~/services";
import { Gesture, GestureDetector } from "react-native-gesture-handler";

function ShelfAndInventoryScreen() {
    const [isShowWarningTooltip, warningTooltipHandlers] = useDisclosure();
    const [isShowCompartmentModal, compartmentModalHandlers] = useDisclosure();
    const [isShowOptionAccordion, optionAccordionHandlers] = useDisclosure();
    const [rowNo, setRowNo] = useState(0);
    const [pressMode] = useState("isShowCompartmentDetails");
    const [selectedItems, setSelectedItems] = useState({ "src": [], "des": [] });
    // const { data, isSuccess, refetch } = useGetMockCompartmentsQuery({ row: rowNo + 1 });
    // const { data: productsData } = useGetMockProductsQuery({});
    const { data, isSuccess, refetch } = useGetCompartmentsQuery({ row: rowNo + 1 });
    const { data: productsData } = useGetProductsQuery({});
    const [fillACompartment, { isSuccess: isFillACompartmentSuccess }] = useFillAMockCompartmentMutation();

    //? Handlers
    const handleShelfLevelTabChange = (value: number) => {
        setRowNo(value);
    }

    const handleCompartmentSinglePress = (compartmentPosition: object) => {

        switch (pressMode) {
            case "isShowCompartmentDetails":
                // @ts-ignore
                compartmentModalHandlers.open(compartmentPosition.compartmentNo)
                break;
            case "isChoosingSrcCompartments":
                break;
            case "isChoosingDesCompartments":
                break;
            default:
                break;
        }
    }

    const handleOption1 = () => {
        fillACompartment({ body: MOCK_STOCKING_PAIRS });
        alert("Option 1 is chosen")
    }

    const singleTapGesture = (compartmentNo: number) => {
        return Gesture.Tap()
            // @ts-ignore
            .onEnd(() => compartmentModalHandlers.open(compartmentNo))
            .runOnJS(true);
    };

    const doubleTapGesture = (message: string) => {
        return Gesture.Tap()
            .numberOfTaps(2)
            .onEnd(() => {
                alert(message)
            })
            .runOnJS(true)
    }


    useEffect(() => {
        if (isFillACompartmentSuccess) refetch();
    }, [isFillACompartmentSuccess]);

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
                    header: props => <CommandBar />,

                }}
            />

            <ScrollView className="container shrink-0 h-full mx-auto px-5 py-2 lg:px-32 lg:pt-12">
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
                <View className={`pl-3 pt-1 ${isShowOptionAccordion === -999 ? 'h-[36%]' : 'h-[35%]'} flex flex-row flex-wrap border-dashed border-2 border-black mb-2`}>
                    {isSuccess &&
                        Array.from({ length: 100 }, (_, i) => i).map((_, i) => {
                            let compartment = data.find((compartment: { position: { compartmentNo: number } }) => compartment.position.compartmentNo === i)
                            let compartmentStatus = compartment?.productDetails?.status;
                            if ([3, 4, 5, 6, 7, 13, 17, 23, 27, 33, 34, 35, 36, 37, 38, 39, 43, 44, 45, 46, 47, 48, 49, 53, 54, 55, 56, 57, 58, 59, 63, 67, 73, 77, 83, 87, 93, 94, 95, 96, 97].includes(i)) return (<Pressable disabled={true} onPress={() => { }} key={i} className={`flex w-[8%] h-[8%] ml-1 mb-1 rounded-lg`} />)
                            else return (

                                <Pressable
                                    onLongPress={() => alert(i)}
                                    key={i} className={`flex w-[8%] h-[8%] border-solid border-4 border-black ml-1 mb-1 rounded-lg 
                                ${compartment?.hasOwnProperty('productDetails') ? 'bg-gray-400' : 'bg-[url(https://st2.depositphotos.com/2009363/8591/v/950/depositphotos_85916628-stock-illustration-the-gray-and-white-squares.jpg)] bg-cover'}`}>
                                    <GestureDetector gesture={Gesture.Exclusive(doubleTapGesture('You double tapped'), singleTapGesture(i))}>
                                        <View className="block z-[0] w-full h-full p-1 md:p-2">
                                            <Text data-twe-toggle="tooltip" className="flex items-center text-[8px] h-full w-full absolute left-[2px]">
                                                {compartment.position.compartmentNoFromUserPerspective}
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
                                                {<Text>{compartmentStatus === 2 ? "📅 1 day before the date → Needs to be taken down" : compartmentStatus === 3 ? "💲1 week before the date → Needs to be displayed in the front compartments, at eye level with a discount or 'buy 2 get 1 free' promo" : "🔖 Event time (e.g., The Red Festival) → Needs to be gathered into event shelf"}</Text>}
                                            </>
                                        }
                                        // @ts-ignore
                                        onClose={warningTooltipHandlers.close}
                                        placement="bottom"

                                    >
                                        {compartment?.productDetails?.hasOwnProperty('status') && <FontAwesome
                                            // @ts-ignore
                                            onPress={() => warningTooltipHandlers.open(compartment.position.compartmentNo)} name="warning" size={12} className="z-[10] text-yellow-400 absolute -top-5 -right-1" />}
                                    </Tooltip>
                                </Pressable>

                            )
                        }
                        )
                    }
                </View>
                {/* Json Viewer */}
                <ListItem.Accordion containerStyle={{ backgroundColor: 'yellow' }}
                    content={
                        <>
                            <Text className="text-center text-xl">[TEMPORARY] Stocking Shelves (manual)</Text>
                        </>
                    }
                    isExpanded={isShowOptionAccordion === -111 ? true : false}
                    //  @ts-ignore 
                    onPress={() => optionAccordionHandlers.toggle()}
                >
                    {/* @ts-ignore */}
                    {/* HINT: Khi demo, nhập thêm dữ liệu vào mock data để cho thấy khả năng xếp với số lượng lớn */}
                    <JSONTree hideRoot={true} shouldExpandNode={() => true} data={MOCK_STOCKING_PAIRS} />
                    <ProductTable productsData={productsData} />
                </ListItem.Accordion>
                {/* Inventory  */}
                {isShowOptionAccordion === -999 && <ProductTable productsData={productsData} />}
            </ScrollView >
        </>
    );
}

export default ShelfAndInventoryScreen;