import { Entypo, FontAwesome } from '@expo/vector-icons'
import { Tabs } from 'expo-router'
export default function TabsLayout() {
    return (
        <Tabs
            screenOptions={{
                tabBarActiveTintColor: '#000000',
            }}
        >
            <Tabs.Screen
                name="profile"
                options={{
                    tabBarIcon: ({ color }) => <FontAwesome name="user" size={24} color={color} />,
                }}
            />

            <Tabs.Screen
                name="productOnCompartment"
                options={{
                    tabBarIcon: ({ color }) => <FontAwesome name="dropbox" size={24} color={color} />,
                }}
            />
        </Tabs>
    )
}