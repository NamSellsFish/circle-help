import { PropsWithChildren } from 'react'
import { Text, View } from 'react-native'
import { formatNumber } from '~/utils'

type ProductPriceProps = PropsWithChildren<{ quantity: number, price: number, wholesalePrice: number }>

export default function ProductPrice({ quantity, price, wholesalePrice }: ProductPriceProps) {

    //? Render(s)
    return (
        <View>
            <View className="flex flex-row items-center self-end">
                <Text className="text-sm text-gray-700">
                    {formatNumber(price)}
                </Text>
                <Text className="ml-1">$</Text>
            </View>

            <View className="flex flex-row">
                <Text className="ml-2 text-sm text-gray-500 line-through">
                    {formatNumber(wholesalePrice)}
                    <Text className="ml-1">$</Text>
                </Text>
            </View>
        </View>
    )
}
