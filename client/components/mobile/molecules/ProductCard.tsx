import { PropsWithChildren, useRef, useState } from "react"
import { Pressable, View, Text, Alert } from "react-native"
import { Depot, Icons, ProductPrice, ResponsiveImage } from "~/components"
import { truncate } from "~/utils"
import { Badge } from '@rneui/themed';
import { CompartmentPosition, Product } from "~/types"
import React from "react";
import { Gesture, GestureDetector } from 'react-native-gesture-handler'
import { addToCart, removeFromCart, switchStatus } from "~/store";
import { useAppDispatch, useAppSelector } from "~/hooks";

type ProductCardProps = PropsWithChildren<{ data: Product }>

export default function ProductCard({ data }: ProductCardProps) {

    const dispatch = useAppDispatch()
    //@ts-ignore
    const { items: selectedItems, status, foundItems } = useAppSelector(state => state.cart)
    const productPositionInCartSrc = selectedItems['src'].findIndex((i: { "sku": string, "packageID": number }) => JSON.stringify(i) === JSON.stringify({ "sku": data.sku, "packageID": data.packageID }))

    const singleTapGesture = (() => {
        return Gesture.Tap()
            .onEnd(() => {
                if (data.expired) { return Alert.alert('Product is expired or unavailable') }
                if (status === 'des') { dispatch(switchStatus()) }
                if (productPositionInCartSrc !== -1) {
                    dispatch(removeFromCart({ "sku": data.sku, "packageID": data.packageID }))

                } else {

                    dispatch(addToCart({ "sku": data.sku, "packageID": data.packageID }))
                }
            }
            )
            .runOnJS(true);
    });

    const doubleTapGesture = (message: string) => {
        return Gesture.Tap()
            .numberOfTaps(2)
            .onEnd(() => {
                alert('You double tapped')
            })
            .runOnJS(true)
    }

    return (
        <>
            <View className={`${foundItems.findIndex(item => JSON.stringify(item).includes(JSON.stringify(data))) !== -1 ? 'border-blue-500' : 'border-transparent'} border-2 border-solid`}>
                <GestureDetector gesture={Gesture.Exclusive(singleTapGesture())}>
                    <Pressable className="py-2 border-b border-gray-100 relative w-[80%]">
                        <View className="absolute top-0 left-0 z-10">
                            <View className="bg-gray-500 px-2 py-1 overflow-hidden rounded-md">
                                <Text className="text-white">{data.sku} - Pkg {data.packageID}</Text>
                                {productPositionInCartSrc !== -1 && <Text className="text-center text-red-500">{productPositionInCartSrc}</Text>}
                            </View>
                        </View>

                        <View className="flex flex-row items-center justify-center space-x-1">
                            <View className="flex flex-col p-1 w-[60%]">
                                <ResponsiveImage
                                    dimensions="h-[28vw] w-[30vw] mb-1"
                                    source={data.imageUrl}
                                    alt={data.name}
                                />
                                <View className="mr-3">
                                    <Badge status="error" containerStyle={{ width: "100%" }} textStyle={{ fontSize: 7 }} value={`Expriration date: ${data.expirationDate}`} />
                                </View>
                            </View>
                            <View className="flex-1 flex-wrap space-y-2 w-full">
                                <Text className="inline-block text-xl leading-6 text-gray-800 break-all h-5 w-[150%] ">
                                    {truncate(data.name, 70)}
                                </Text>
                                <View className="flex flex-col justify-start ">
                                    <View>
                                        <Depot quantity={data.quantity} />
                                    </View>
                                    <View className="flex flex-row items-center gap-x-1">
                                        {data.categories && data.categories.map((item: string, index) => <Badge value={item} key={index} />)}
                                    </View>
                                </View>
                                <View className="flex flex-row self-end justify-between">

                                    {data.quantity !== 0 ? (
                                        <ProductPrice
                                            quantity={data.quantity}
                                            price={data.price}
                                            wholesalePrice={data.wholesalePrice}
                                        />
                                    ) : (
                                        <Text className="h-12 my-0.5">Unavailable</Text>
                                    )}
                                </View>
                            </View>
                        </View>
                    </Pressable>
                </GestureDetector>
            </View>
        </>
    )
}