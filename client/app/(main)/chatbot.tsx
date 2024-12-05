import { View, Text, Image } from 'react-native'
import React from 'react'
import { Bubble, GiftedChat, InputToolbar, Send } from 'react-native-gifted-chat'
import { useState } from 'react';
import { useEffect } from 'react';
import { useCallback } from 'react';
import * as GoogleGenerativeAI from "@google/generative-ai";
import { FontAwesome } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { CHAT_FACE_DATA, GEMINI_API_KEY } from '~/constants';
import { Router, Stack } from "expo-router";

function ChatScreen() {
    const [messages, setMessages] = useState<Array<{ _id: number; text: string; createdAt: Date; user: { _id: number; name: string; avatar: string; } }>>([]);
    const [loading, setLoading] = useState(false);
    const [chatFaceColor, setChatFaceColor] = useState('');
    const CHAT_BOT_FACE = 'https://res.cloudinary.com/dknvsbuyy/image/upload/v1685678135/chat_1_c7eda483e3.png';

    useEffect(() => {
        checkFaceId();
    }, [])

    const checkFaceId = async () => {
        const id = 3;
        setChatFaceColor(CHAT_FACE_DATA[id].primary);
        setMessages([
            {
                _id: 3,
                text: 'Hello, I am ' + CHAT_FACE_DATA[id].name + ', How Can I help you?',
                createdAt: new Date(),
                user: {
                    _id: 3,
                    name: 'React Native',
                    avatar: CHAT_BOT_FACE,
                },

            },
        ])
    }

    const onSend = useCallback((messages = []) => {

        setMessages(previousMessages => GiftedChat.append(previousMessages, messages))
        if (messages[0].text) {
            getGeminiResp(messages[0].text);
        }
    }, [])

    const onPress = async () => {
        const genAI = new GoogleGenerativeAI.GoogleGenerativeAI(GEMINI_API_KEY);
        const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
        const prompt = 'hi';
        const result = await model.generateContent(prompt);
        const response = result.response;
        const respTxt = response.text();
        alert(respTxt)
    }

    const getGeminiResp = async (msg: string) => {
        setLoading(true)
        const genAI = new GoogleGenerativeAI.GoogleGenerativeAI(GEMINI_API_KEY);
        const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

        const prompt = msg;
        const result = await model.generateContent(prompt);
        const response = result.response;
        const respTxt = response.text();
        if (respTxt) {
            setLoading(false)
            const chatAIResp = {
                _id: Math.random() * (9999999 - 1),
                text: respTxt,
                createdAt: new Date(),
                user: {
                    _id: 2,
                    name: 'React Native',
                    avatar: CHAT_BOT_FACE,

                }
            }
            setMessages(previousMessages => GiftedChat.append(previousMessages, chatAIResp))

        }
        else {
            setLoading(false)
            const chatAIResp = {
                _id: Math.random() * (9999999 - 1),
                text: "Sorry, I can not help with it",
                createdAt: new Date(),
                user: {
                    _id: 2,
                    name: 'React Native',
                    avatar: CHAT_BOT_FACE,

                }
            }
            setMessages(previousMessages => GiftedChat.append(previousMessages, chatAIResp))
        }

    }

    const renderBubble = (props) => {
        return (
            <Bubble
                {...props}
                wrapperStyle={{
                    right: {
                        backgroundColor: '#671ddf',

                    }, left: {

                    }

                }}
                textStyle={{
                    right: {
                        // fontSize:20,
                        padding: 2
                    },
                    left: {
                        color: '#3B96D2',
                        // fontSize:20,
                        padding: 2
                    }
                }}
            />
        )
    }

    const renderInputToolbar = (props) => {
        //Add the extra styles via containerStyle
        return <InputToolbar {...props}
            containerStyle={{
                padding: 3,

                backgroundColor: '#3B96D2',
                color: '#fff',
            }}

            textInputStyle={{ color: "#fff" }}
        />
    }

    const renderSend = (props) => {
        return (
            <Send
                {...props}
            >
                <View style={{ marginRight: 10, marginBottom: 5 }}>
                    <FontAwesome name="send" size={24} color="white" resizeMode={'center'} />

                </View>
            </Send>
        );
    }
    return (
        <>
            <Stack.Screen
                options={{
                    title: 'Chat Bot',
                    headerShown: true,
                }} /><View style={{ flex: 1, backgroundColor: '#fff' }}>

                <GiftedChat
                    messages={messages}
                    isTyping={loading}
                    onSend={messages => onSend(messages)}
                    user={{
                        _id: 1,
                    }}
                    renderBubble={renderBubble}
                    renderInputToolbar={renderInputToolbar}
                    renderSend={renderSend} />


            </View>
        </>
    )
}
export default ChatScreen;