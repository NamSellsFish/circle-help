import { useWindowDimensions, View, Text, ScrollView } from "react-native"
import { transformKey } from "~/utils"

type SpecificationProps = { specification: object }

export default function Specification({ specification }: SpecificationProps) {
    //? Assets
    const { width } = useWindowDimensions()


    return (
        <View className="px-4 pb-5">
            <View className="lg:max-w-3xl xl:max-w-5xl">
                <View className="l">
                    <ScrollView className="space-y-4">
                        {
                            // Object.entries(specification).map(
                            //     ([k, v], i) =>
                            //         <Text>{JSON.stringify(k)}-{JSON.stringify(v)}</Text>

                            // )

                            Object.entries(specification).map(
                                ([k, v], i) => {

                                    if (!v || k === 'compartmentNo') return
                                    else
                                        return (
                                            <View key={i} className="flex flex-row">
                                                <Text className="py-2 capitalize font-light leading-5 tracking-wide text-gray-500 w-20">{transformKey(k === 'compartmentNoFromUserPerspective' ? 'compartmentNo' : k)}</Text>
                                                <View className="flex-auto block w-full py-2 font-normal leading-5 tracking-wide text-gray-600 break-all">
                                                    {typeof v === 'object' ? <Specification specification={v} /> : <Text>{v}</Text>}
                                                </View>
                                            </View>
                                        )
                                }
                            )
                        }
                        {/* <Text>{JSON.stringify(specification)}</Text> */}
                    </ScrollView>
                </View>
            </View>
        </View>
    )
}

