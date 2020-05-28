/*
 * Copyright © 2020 FUGA (mark.schenk@fuga.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const tips = [
'Hey, why mock?',
'fixedDelayMilliseconds can delay the response :)',
'Url pattern /a\\?.* can help solve something :)',
'Context can help you work independency',
'Can export all your mock data to files',
'Boring with light? Change to dark mode!',
'Enable/Disable mocking for easy switching',
'Upload your mocking files so easy than ever before',
'Hey, Just search something',
'Boring with JSON color? Change it!',
'Expand layout for more space',
'Collapse layout for fun',
'Tired real API issue, let mock!',
'Refresh mocking engine by clicking on top right button',
'Copy CURL for using later',
'Duplidate mocking things for alternative cases',
'Share mocking things to another context',
'Boring with your context, Jump to another one',
'Hacking OpenAPI Mock? OK, I am fine!',
];

/**
 * Returns a random number between min (inclusive) and max (exclusive)
 */
const getRandomInt = (minimum, maximum) => {
    return Math.floor(Math.random() * (maximum - minimum)) + minimum;
}

export const getTip = ()=>{
    const i = getRandomInt(0,tips.length);
    console.log('sax',i);
    return tips[i];
}
