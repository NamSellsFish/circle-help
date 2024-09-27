import { Stack } from "expo-router";
import { Image, Pressable, Text, View } from 'react-native'

const DATA = [
    { title: "First Item" },
    { title: "Second Item" },
    { title: "Item 3" },
    { title: "Item 4" },
    { title: "Item 5" },
    { title: "Item 6" },
    { title: "Item 7" },
    { title: "Item 8" },
    { title: "Item 9" },
    { title: "Item 10" },
    { title: "Item 11" },
    { title: "Item 12" },
    { title: "Item 13" },
    { title: "Item 14" },
    { title: "Item 15" },
    { title: "Item 16" },
    { title: "Item 17" },
    { title: "Item 18" },
    { title: "Item 19" },
    { title: "Item 20" },
    { title: "Item 21" },
    { title: "Item 22" },
    { title: "Item 23" },
    { title: "Item 24" },
    { title: "Item 25" },
    { title: "Item 26" },
    { title: "Item 27" },
    { title: "Item 28" },
    { title: "Item 29" },
    { title: "Item 30" },
    { title: "Item 31" },
    { title: "Item 32" },
    { title: "Item 33" },
    { title: "Item 34" },
    { title: "Item 35" },
    { title: "Item 36" },
    { title: "Item 37" },
    { title: "Item 38" },
    { title: "Item 39" },
    { title: "Item 40" },
    { title: "Item 41" },
    { title: "Item 42" },
    { title: "Item 43" },
    { title: "Item 44" },
    { title: "Item 45" },
    { title: "Item 46" },
    { title: "Item 47" },
    { title: "Item 48" },
    { title: "Item 49" },
    { title: "Item 50" },
    { title: "Item 51" },
    { title: "Item 52" },
    { title: "Item 53" },
    { title: "Item 54" },
    { title: "Item 55" },
    { title: "Item 56" },
    { title: "Item 57" },
    { title: "Item 58" },
    { title: "Item 59" },
    { title: "Item 60" },
    { title: "Item 61" },
    { title: "Item 62" },
    { title: "Item 63" },
    { title: "Item 64" },
    { title: "Item 65" },
    { title: "Item 66" },
    { title: "Item 67" },
    { title: "Item 68" },
    { title: "Item 69" },
    { title: "Item 70" },
    { title: "Item 67" },
    { title: "Item 68" },
    { title: "Item 69" },
    { title: "Item 70" },
    { title: "Item 70" },
];


function StockScreen() {
    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: true,
                }}
            />
            <View className=" flex flex-row justify-center flex-wrap border-solid border-2 border-red-400 m-2">
                {/* 
                - Tool tip chỉ hiển thị warning/suggest message
                - Còn Modal hiển thị form chi tiết hàng trên kệ 
                */}
                {DATA.map((item, index) => (
                    <Pressable key={index} className="flex items-center w-1/6 border-solid border-2 border-blue-200 m-1">
                        <Text className="text-gray-700 ">{`#${index}`}</Text>
                    </Pressable>
                ))}
            </View>
        </>
    );
}

export default StockScreen;