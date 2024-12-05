import { Text, View } from 'react-native'
import { formatNumber } from '~/utils'
import { PropsWithChildren } from 'react'
import { Icons } from '~/components'

type DepotProps = PropsWithChildren<{ quantity: number }>

export default function Depot({ quantity }: DepotProps) {
    //? Render(s)
    if (quantity < 6 && quantity !== 1) {
        return <Text className="text-yellow-500">Only {formatNumber(quantity)} items left! </Text>
    } else if (quantity >= 6) {
        return (
            <View className="flex flex-row items-center text-teal-400 gap-x-1 w-full">
                <Icons.FontAwesome name="save" size={16} className="text-teal-500" />
                <Text className="text-teal-700">{formatNumber(quantity)} items available.</Text>
            </View>
        )
    } else if (quantity === 1) {
        return <Text className="text-red-500">Only 1 item left!!!</Text>
    }
}
