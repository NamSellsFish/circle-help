import { FontAwesome, MaterialCommunityIcons } from '@expo/vector-icons'
import { Tabs } from 'expo-router'
export default function TabsLayout() {
    return (
        <Tabs
            screenOptions={{
                tabBarActiveTintColor: '#000000',
            }}
        >
            <Tabs.Screen
                name="attendanceTracking"
                options={{
                    tabBarLabel: "Attendance",
                    tabBarIcon: ({ color }) => <MaterialCommunityIcons name="gesture-double-tap" size={24} color={color} />,
                }}
            />

            <Tabs.Screen
                name="index"
                options={{
                    tabBarLabel: "Profile",
                    tabBarIcon: ({ color }) => <FontAwesome name="user" size={24} color={color} />,
                }}
            />

            <Tabs.Screen
                name="shelfAndInventory"
                options={{
                    tabBarLabel: "Shelf & Inventory",
                    tabBarIcon: ({ color }) => <FontAwesome name="dropbox" size={24} color={color} />,
                }}
            />
        </Tabs>
    )
}