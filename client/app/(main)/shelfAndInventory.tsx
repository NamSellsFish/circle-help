import { FontAwesome } from "@expo/vector-icons";
import { Stack } from "expo-router";
import React from "react";
import { Pressable, ScrollView, Text, View } from 'react-native'
import Tooltip from 'react-native-walkthrough-tooltip';
import { CompartmentModal } from "~/components";

// Mock data
const MOCK_RESPONSE_BODY = [
    { id: 1, product: 'product 1', price: 100, expireDate: '2022-12-12' },
    { id: 2, product: 'product 2', price: 101, expireDate: '2022-12-13' },
    { id: 3, product: 'product 3', price: 102, expireDate: '2022-12-14' },
    { id: 4, product: 'product 4', price: 103, expireDate: '2022-12-15' },
    { id: 5, product: 'product 5', price: 104, expireDate: '2022-12-16' },
    { id: 6, product: 'product 6', price: 105, expireDate: '2022-12-17' },
    { id: 7, product: 'product 7', price: 106, expireDate: '2022-12-18' },
    { id: 8, product: 'product 8', price: 107, expireDate: '2022-12-19' },
    { id: 9, product: 'product 9', price: 108, expireDate: '2022-12-20' },
    { id: 10, product: 'product 10', price: 109, expireDate: '2022-12-21' },
    { id: 11, product: 'product 11', price: 110, expireDate: '2022-12-22' },
    { id: 12, product: 'product 12', price: 111, expireDate: '2022-12-23' },
    { id: 13, product: 'product 13', price: 112, expireDate: '2022-12-24' },
    { id: 14, product: 'product 14', price: 113, expireDate: '2022-12-25' },
    { id: 15, product: 'product 15', price: 114, expireDate: '2022-12-26' },
    { id: 16, product: 'product 16', price: 115, expireDate: '2022-12-27' },
    { id: 17, product: 'product 17', price: 116, expireDate: '2022-12-28' },
    { id: 18, product: 'product 18', price: 117, expireDate: '2022-12-29' },
    { id: 19, product: 'product 19', price: 118, expireDate: '2022-12-30' },
    { id: 20, product: 'product 20', price: 119, expireDate: '2022-12-31' },
    { id: 21, product: 'product 21', price: 120, expireDate: '2023-01-01' },
    { id: 22, product: 'product 22', price: 121, expireDate: '2023-01-02' },
    { id: 23, product: 'product 23', price: 122, expireDate: '2023-01-03' },
    { id: 24, product: 'product 24', price: 123, expireDate: '2023-01-04' },
    { id: 25, product: 'product 25', price: 124, expireDate: '2023-01-05' },
    { id: 26, product: 'product 26', price: 125, expireDate: '2023-01-06' },
    { id: 27, product: 'product 27', price: 126, expireDate: '2023-01-07' },
    { id: 28, product: 'product 28', price: 127, expireDate: '2023-01-08' },
    { id: 29, product: 'product 29', price: 128, expireDate: '2023-01-09' },
    { id: 30, product: 'product 30', price: 129, expireDate: '2023-01-10' },
    { id: 31, product: 'product 31', price: 130, expireDate: '2023-01-11' },
    { id: 32, product: 'product 32', price: 131, expireDate: '2023-01-12' },
    { id: 33, product: 'product 33', price: 132, expireDate: '2023-01-13' },
    { id: 34, product: 'product 34', price: 133, expireDate: '2023-01-14' },
    { id: 35, product: 'product 35', price: 134, expireDate: '2023-01-15' },
    { id: 36, product: 'product 36', price: 135, expireDate: '2023-01-16' },
    { id: 37, product: 'product 37', price: 136, expireDate: '2023-01-17' },
    { id: 38, product: 'product 38', price: 137, expireDate: '2023-01-18' },
    { id: 39, product: 'product 39', price: 138, expireDate: '2023-01-19' },
    { id: 40, product: 'product 40', price: 139, expireDate: '2023-01-20' },
    { id: 41, product: 'product 41', price: 140, expireDate: '2023-01-21' },
    { id: 42, product: 'product 42', price: 141, expireDate: '2023-01-22' },
    { id: 43, product: 'product 43', price: 142, expireDate: '2023-01-23' },
    { id: 44, product: 'product 44', price: 143, expireDate: '2023-01-24' },
    { id: 45, product: 'product 45', price: 144, expireDate: '2023-01-25' },
    { id: 46, product: 'product 46', price: 145, expireDate: '2023-01-26' },
    { id: 47, product: 'product 47', price: 146, expireDate: '2023-01-27' },
    { id: 48, product: 'product 48', price: 147, expireDate: '2023-01-28' },
    { id: 49, product: 'product 49', price: 148, expireDate: '2023-01-29' },
    { id: 50, product: 'product 50', price: 149, expireDate: '2023-01-30' },
    { id: 51, product: 'product 51', price: 150, expireDate: '2023-01-31' },
    { id: 52, product: 'product 52', price: 151, expireDate: '2023-02-01' },
    { id: 53, product: 'product 53', price: 152, expireDate: '2023-02-02' },
    { id: 54, product: 'product 54', price: 153, expireDate: '2023-02-03' },
    { id: 55, product: 'product 55', price: 154, expireDate: '2023-02-04' },
    { id: 56, product: 'product 56', price: 155, expireDate: '2023-02-05' },
    { id: 57, product: 'product 57', price: 156, expireDate: '2023-02-06' },
    { id: 58, product: 'product 58', price: 157, expireDate: '2023-02-07' },
    { id: 59, product: 'product 59', price: 158, expireDate: '2023-02-08' },
    { id: 60, product: 'product 60', price: 159, expireDate: '2023-02-09' },
    { id: 61, product: 'product 61', price: 160, expireDate: '2023-02-10' },
    { id: 62, product: 'product 62', price: 161, expireDate: '2023-02-11' },
    { id: 63, product: 'product 63', price: 162, expireDate: '2023-02-12' },
    { id: 64, product: 'product 64', price: 163, expireDate: '2023-02-13' },
    { id: 65, product: 'product 65', price: 164, expireDate: '2023-02-14' },
    { id: 66, product: 'product 66', price: 165, expireDate: '2023-02-15' },
    { id: 67, product: 'product 67', price: 166, expireDate: '2023-02-16' },
    { id: 68, product: 'product 68', price: 167, expireDate: '2023-02-17' },
    { id: 69, product: 'product 69', price: 168, expireDate: '2023-02-18' },
    { id: 70, product: 'product 70', price: 169, expireDate: '2023-02-19' },
    { id: 71, product: 'product 71', price: 170, expireDate: '2023-02-20' },
    { id: 72, product: 'product 72', price: 171, expireDate: '2023-02-21' },
    { id: 73, product: 'product 73', price: 172, expireDate: '2023-02-22' },
    { id: 74, product: 'product 74', price: 173, expireDate: '2023-02-23' },
    { id: 75, product: 'product 75', price: 174, expireDate: '2023-02-24' },
    { id: 76, product: 'product 76', price: 175, expireDate: '2023-02-25' },
    { id: 77, product: 'product 77', price: 176, expireDate: '2023-02-26' },
    { id: 78, product: 'product 78', price: 177, expireDate: '2023-02-27' },
    { id: 79, product: 'product 79', price: 178, expireDate: '2023-02-28' },
    { id: 80, product: 'product 80', price: 179, expireDate: '2023-03-01' },
    { id: 81, product: 'product 81', price: 180, expireDate: '2023-03-02' },
    { id: 82, product: 'product 82', price: 181, expireDate: '2023-03-03' },
    { id: 83, product: 'product 83', price: 182, expireDate: '2023-03-04' },
    { id: 84, product: 'product 84', price: 183, expireDate: '2023-03-05' },
    { id: 85, product: 'product 85', price: 184, expireDate: '2023-03-06' },
    { id: 86, product: 'product 86', price: 185, expireDate: '2023-03-07' },
    { id: 87, product: 'product 87', price: 186, expireDate: '2023-03-08' },
    { id: 88, product: 'product 88', price: 187, expireDate: '2023-03-09' },
    { id: 89, product: 'product 89', price: 188, expireDate: '2023-03-10' },
    { id: 90, product: 'product 90', price: 189, expireDate: '2023-03-11' },
    { id: 91, product: 'product 91', price: 190, expireDate: '2023-03-12' },
    { id: 92, product: 'product 92', price: 191, expireDate: '2023-03-13' },
    { id: 93, product: 'product 93', price: 192, expireDate: '2023-03-14' },
    { id: 94, product: 'product 94', price: 193, expireDate: '2023-03-15' },
    { id: 95, product: 'product 95', price: 194, expireDate: '2023-03-16' },
    { id: 96, product: 'product 96', price: 195, expireDate: '2023-03-17' },
    { id: 97, product: 'product 97', price: 196, expireDate: '2023-03-18' },
    { id: 98, product: 'product 98', price: 197, expireDate: '2023-03-19' },
    { id: 99, product: 'product 99', price: 198, expireDate: '2023-03-20' },
    { id: 100, product: 'product 100', price: 100, expireDate: '2023-03-21' },
];
const MOCK_MODAL_STATE = 101;
const MOCK_TOOLTIP_STATE = 101;


function ShelfAndInventoryScreen() {

    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: false,
                }}
            />
            {/* Shelf-level Tab list */}
            <ScrollView className="container order-first flex-grow-0 h-10 bg-red-300 mx-auto px-5 py-2 lg:px-32 lg:pt-12" />

            {/* Compartment Gallery */}
            <ScrollView className="container shrink-0 mx-auto px-5 py-2 lg:px-32 lg:pt-12 ">
                <View className="pl-3 pt-1 h-[200%] flex flex-row flex-wrap border-dashed border-2 border-black">
                    {
                        Array.from({ length: 100 }, (_, i) => i).map((item, i) => {
                            if ([3, 4, 5, 6, 7, 13, 17, 23, 27, 33, 34, 35, 36, 37, 38, 39, 43, 44, 45, 46, 47, 48, 49, 53, 54, 55, 56, 57, 58, 59, 63, 67, 73, 77, 83, 87, 93, 94, 95, 96, 97].includes(i)) return (<Pressable disabled={true} onPress={() => { }} key={i} className={`flex w-[8%] h-[8%] ml-1 mb-1 rounded-lg`} />)
                            else return (                                                                                                                   // TODO: Cần quản lý bằng state
                                <Pressable onPress={() => { }} key={i} className={`flex w-[8%] h-[8%] border-solid border-4 border-black ml-1 mb-1 rounded-lg ${item === 41 ? 'bg-gray-400' : 'bg-[url(https://st2.depositphotos.com/2009363/8591/v/950/depositphotos_85916628-stock-illustration-the-gray-and-white-squares.jpg)] bg-cover'}`}>
                                    <View className="block z-[0] w-full h-full p-1 md:p-2">
                                        <Text data-twe-toggle="tooltip" className="flex items-center text-[8px] h-full w-full absolute left-[2px]">
                                            {i}
                                        </Text>
                                        <CompartmentModal isShow={MOCK_MODAL_STATE === item} onClose={() => { }} data={{}} />
                                        {
                                            // TODO: Cần quản lý bằng state
                                            [5, 12, 23, 41, 37, 86, 75, 59, 30, 92, 33].includes(item)
                                            &&
                                            <Tooltip
                                                arrowSize={{ width: 16, height: 8 }}
                                                isVisible={MOCK_TOOLTIP_STATE === item}
                                                disableShadow
                                                content={

                                                    <>
                                                        {/* <Text className="text-lg font-bold">📌 Tips:</Text> */}
                                                        <Text>📅 1 day before the date → Needs to be taken down </Text>
                                                        <Text>🔖 Event time (e.g., The Red Festival) → Needs to be gathered into event shelf </Text>
                                                        <Text>💲1 week before the date → Needs to be displayed in the front compartments with a discount or 'buy 2 get 1 free' promo </Text>
                                                    </>


                                                }
                                                onClose={() => { }}
                                                placement="bottom"

                                            >

                                                <FontAwesome onPress={() => { }} name="warning" size={12} className="z-[10] text-yellow-400 absolute -top-1 -right-1" />
                                            </Tooltip>
                                        }
                                    </View>
                                </Pressable>

                            )
                        })
                    }


                </View>

            </ScrollView >

            {/* Inventory */}
            <ScrollView className="container order-last flex-grow-0 h-0 bg-green-300" />

        </>
    );
}

export default ShelfAndInventoryScreen;