import { PropsWithChildren, useRef, useState } from "react"
import { Pressable, View, Text, Alert } from "react-native"
import { Depot, Icons, ProductPrice, ResponsiveImage } from "~/components"
import { truncate } from "~/utils"
import { Badge } from '@rneui/themed';
import { Product } from "~/types"
import React from "react";
import { Gesture, GestureDetector } from 'react-native-gesture-handler'

type ProductCardProps = PropsWithChildren<{ data: Product }>

export default function ProductCard({ data }: ProductCardProps) {

    const handleTap = (message: string) => () => {
        alert(message);
    };

    const singleTapGesture = (message: string) => {
        return Gesture.Tap()
            .onEnd(handleTap(message))
            .runOnJS(true);
    };

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
            <GestureDetector gesture={Gesture.Exclusive(doubleTapGesture('You double tapped'), singleTapGesture('You single tapped'))}>
                <Pressable className="py-2 border-b border-gray-100 relative w-[80%]">
                    <View className="absolute top-0 left-0 z-10">
                        <View className="bg-gray-500 px-2 py-1 overflow-hidden rounded-md">
                            <Text className="text-white">{data.sku}</Text>
                        </View>
                    </View>

                    <View className="flex flex-row items-center justify-center space-x-1">
                        <View className="flex flex-col p-1 w-[60%] ">
                            <ResponsiveImage
                                dimensions="h-[28vw] w-[30vw] mb-1 "
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
                                        wholesalePrice={data.wholesalePrice}
                                        price={data.price}
                                    />
                                ) : (
                                    <Text className="h-12 my-0.5">Unavailable</Text>
                                )}
                            </View>
                        </View>
                    </View>
                </Pressable>
            </GestureDetector>
        </>
    )
}