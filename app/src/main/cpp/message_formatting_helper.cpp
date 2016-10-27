//
// Created by Neil Connatty on 2016-10-27.
//

#include "message_formatting_helper.h"
using namespace messaging;

std::string formatting_helper::format_message (std::string msg) {
     return "\"" + msg + "\"";
}