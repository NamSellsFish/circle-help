import { useWindowDimensions, View, Text } from "react-native"

type SpecificationProps = { specification: object }

export default function Specification({ specification }: SpecificationProps) {
    //? Assets
    const { width } = useWindowDimensions()


    return (
        <View className="px-4 pb-5">
            <View className="lg:max-w-3xl xl:max-w-5xl">
                {/* <Text className="mb-1 h-fit w-fit"></Text> */}
                <View className="l">
                    <View className="space-y-4">
                        {
                            Object.entries(specification).map(
                                ([k, v], i) => {
                                    if (!v) return
                                    else
                                        return (
                                            <View key={i} className="flex flex-row">
                                                <Text className="py-2 ml-3 capitalize font-light leading-5 tracking-wide text-gray-500 w-36">
                                                    {k}
                                                </Text>
                                                <View className="flex-auto block w-full py-2 font-normal leading-5 tracking-wide text-gray-600 break-all">
                                                    {`${v}`}
                                                </View>
                                            </View>
                                        )
                                }
                            )
                        }
                    </View>
                </View>
            </View>
        </View>
    )
}