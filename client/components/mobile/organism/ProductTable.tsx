import { FlashList } from "@shopify/flash-list"
import { useLocalSearchParams } from "expo-router"
import React from "react"
import { PropsWithChildren } from "react"
import { View, Text, Pressable } from "react-native"
import { ProductCard } from "~/components"
import { useChangeRoute } from "~/hooks"

type ProductTableProps = PropsWithChildren<{ productsData: object[], page: object }>

export default function ProductTable({ productsData, page }: ProductTableProps) {

    const params = useLocalSearchParams()
    const changeRoute = useChangeRoute()

    const onEndReachedThreshold = () => {
        if (page.totalPages === 1) return
        changeRoute({ page: page.number < page.totalPages - 1 ? Number(page.number) + 1 : params.page })
    }

    return (
        <>

            <Pressable onPress={() => changeRoute({ page: '' })} id="_products" className="w-full h-[77%] flex px-4 pt-2 mt-2 ">
                {/* Products */}
                {productsData && productsData?.length > 0 ? (
                    <FlashList
                        showsVerticalScrollIndicator={true}
                        data={productsData}
                        // @ts-ignore
                        renderItem={({ item }) => <ProductCard data={item} key={`${item.sku}-${item.packageID}`} />}
                        onEndReached={onEndReachedThreshold}
                        estimatedItemSize={148}
                        onEndReachedThreshold={0.01}
                    />
                ) : (
                    <Text className="text-center text-red-500">{`No product available!`}</Text>
                )}
            </Pressable>

        </>
    )
}