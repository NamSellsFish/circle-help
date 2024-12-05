import { FlashList } from "@shopify/flash-list"
import React from "react"
import { PropsWithChildren } from "react"
import { View, Text } from "react-native"
import { ProductCard } from "~/components"
import { Product } from "~/types"

type ProductTableProps = PropsWithChildren<{ productsData: object[] }>

export default function ProductTable({ productsData }: ProductTableProps) {
    return (
        <>
            <View className="px-1 flex-1 ">
                <View id="_products" className="w-full h-[100%] flex px-4 py-2 mt-2 ">
                    {/* Filters & Sort */}
                    <View className="divide-y-2 divide-neutral-200">
                        <View className="flex flex-row py-2 gap-x-3">
                            {/* <Filter
                                    mainMaxPrice={data?.data?.mainMaxPrice}
                                    mainMinPrice={data?.data?.mainMinPrice}
                                    handleChangeRoute={handleChangeRoute}
                                />
                                <Sort handleChangeRoute={handleChangeRoute} /> */}
                        </View>

                        <View className="flex flex-row justify-between py-2">
                            <Text className="text-base text-neutral-600">All product(s) available</Text>

                            <Text className="text-base text-neutral-600">
                                {productsData?.length} item(s)
                            </Text>
                        </View>
                    </View>
                    {/* Products */}
                    {productsData && productsData?.length > 0 ? (
                        <FlashList
                            showsVerticalScrollIndicator={false}
                            data={productsData}
                            // @ts-ignore
                            renderItem={({ item }) => <ProductCard data={item} key={item.sku} />}
                            onEndReached={() => { }}
                            onEndReachedThreshold={0}
                            estimatedItemSize={200}
                        />
                    ) : (
                        <Text className="text-center text-red-500">No product available!</Text>
                    )}
                </View>
            </View>
        </>
    )
}