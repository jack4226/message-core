// Define Angular Module
var baseApp1 = angular.module('myReuseableMod', []);

baseApp1.factory('myReusableSrvc1', function() {
    // code here
});

baseApp1.factory('myReusableSrvc2', function() {
    // code here
});


var app = angular.module("demoApp", ['myReuseableMod', 'ngRoute']);

// Example of $rootScope
app.run(function($rootScope) {
    $rootScope.myColor = 'blue';
});

app.value("messageLabel", "Message - ");
app.constant("responseLabel", "Response: ");

// Create a service using provider that load during configuration phase.
app.config(function($provide) {
   $provide.provider('MathService', function() {
      this.$get = function() {
         var factory = {};  
         
         factory.multiply = function(a, b) {
            return a * b; 
         }
         return factory;
      };
   });
});


function demoCtrl(messageLabel, responseLabel) {
    this.showMessage = function() {
    	return messageLabel + this.message;
    };
    this.showResponse = function() {
    	return responseLabel + this.response + " - " + this.responseMsg;
    };
	this.title = "AngularJS Demo";
    this.book = "The Alchemist";
    this.author = "Paulo Coelho";
    this.message = "demoCtrl message";
    this.demohref = "select.html";
    this.demosrc = "img/file.png";
    this.response = "error";
    this.responseMsg = "Response message";
    this.showMenu = true;
    this.userExists = true;
    this.access = "admin";
    this.price = "123.45";
};

function userCtrl($filter, messageLabel, $http, $q) {
	this.title = "EDI User Mapping";
    this.entityId = "6276143";
    this.ticket = "EDI-11111";
    this.message = "user mapping message";
    this.mappings = [
    	{
	    	clId : '6123456',
	    	sso : 'sso123456'
	    }, {
	    	clId : '6153427',
	    	sso : 'sso871894'
	    }
    ];
    
    this.reset = function() {
    	//this.entityId = "";
        //this.ticket = "";
        this.mappings = [];
    };
    
    this.addUser = function() {
    	this.mappings.push({
    		clId : '',
	    	sso : ''
    	});
    };
    
    this.removeUser = function(user) {
    	this.mappings.splice(user, 1);
    };
    
    // remove selected records
    this.remove = function() {
		var newDataList = [];
		angular.forEach(this.mappings, function(mapping) {
			if (!mapping.selected) {
				newDataList.push(mapping);
			}
		});
		this.mappings = newDataList;
	};
	
	// check if any CheckBox is selected
	this.rowSelected = function() {
		var trues = $filter("filter")(this.mappings, {
	        selected: true
	    });
	    return trues.length;
	};
	
    this.showMessage = function() {
    	return messageLabel + this.message;
    };
    
    this.submitForm = function($http) {
    	// TO DO
    	console.log("In submit form!");
    	localStorage.setItem('users', JSON.stringify(this.mappings));
    	if (localStorage.getItem('users') != null) {
    		this.savedUsers = JSON.parse(localStorage.getItem('users'));
    	}
    	else {
    		this.savedUsers = [];
    	}
    };
    
    function okToGreet(name) {
    	return true;
    }

    function asyncGreet(name) {
	// perform some asynchronous operation, resolve or reject the promise when appropriate.
		return $q(function(resolve, reject) {
			setTimeout(function() {
				if (okToGreet(name)) {
					resolve('Hello, ' + name + '!');
				} else {
					reject('Greeting ' + name + ' is not allowed.');
				}
			}, 1000);
		});
	}

	var promise = asyncGreet('Robin Hood');
	promise.then(function(greeting) {
		alert('Success: ' + greeting);
	}, function(reason) {
		alert('Failed: ' + reason);
	});
};

function servCtrl(UserService) {
	this.sayHello = function(name) {
		UserService.sayHello(name);
	};
}


function itemCtrl() {
	var vm = this;
	// function to remove an item
	vm.removeFromStock = function(item, index) {
		vm.errortext = "Item removed shopping list.";
		vm.items.splice(index, 1);
	};
	// function to add an item
	vm.addStock = function(item) {
		vm.errortext = "Pushing item to shopping list.";
		vm.newitem = {
			name : item.name,
			id : item.id
		};
		vm.items.push(vm.newitem);
	};
	// define an empty object
	vm.form = {
		name : '',
		id : 0
	};
	
	vm.itemName="Snorkel";
	vm.items = [ {
		name : 'Scuba Diving Kit',
		id : 7297510
	}, {
		name : 'Snorkel',
		id : 0278916
	}, {
		name : 'Wet Suit',
		id : 2389017
	}, {
		name : 'Beach Towel',
		id : 1000983
	} ];
	
	// order by user input
	this.orderByMe = function(x) {
		this.myOrderBy = x;
	};
};


// ajax controllers
function studentController($scope, $http) {
	var url = "data/students.txt";

	$http.get(url).then( function(response) {
		$scope.students = response.data.records;
	});
}

function ajaxCtrl_1($scope, $http) {
	$http.get("header.html").then(function(response) {
		$scope.content = response.data;
    });
}

function ajaxCtrl_2($scope, $http) {
	$http({
        method : "GET",
        url : "header.html"
    }).then(function mySuccess(response) {
    	$scope.content = response.data;
    }, function myError(response) {
    	$scope.content = response.statusText;
    });
}

function ajaxCtrl($scope, $http) {
	$http.get("header.html")
    .then(function(response) {
        $scope.content = response.data;
        $scope.status = response.status;
        $scope.statustext = response.statustext;
        $scope.headers = response.headers;
        $scope.config = response.config;
    });
}

function ajaxCtrl_4($scope, $http) {
	$http.get("header.html")
	.then(function(response) {
        //First function handles success
        $scope.content = response.data;
    }, function(response) {
        //Second function handles error
        $scope.content = "Something went wrong";
    });
}
// End of ajax

// Service controllers
app.controller('getUrlCtrl', function($scope, $location) {
    $scope.myUrl = $location.absUrl();
});

app.controller('myTimeout', function($scope, $timeout) {
	$scope.myHeader = "Hello World!";
	$timeout(function () {
		$scope.myHeader = "How are you today?";
	}, 2000);
});

app.controller('myInterval', function($scope, $interval) {
    $scope.theTime = new Date().toLocaleTimeString();
    $interval(function () {
        $scope.theTime = new Date().toLocaleTimeString();
    }, 1000);
});

app.service('hexafy', function() {
    this.toHexString = function (x) {
        return x.toString(16);
    }
});

app.controller('myHexafy', function($scope, hexafy) {
  $scope.hex = hexafy.toHexString(255);
});

// Inject factory to service to controller
// replaced by same service using $provider
app.factory('MathService_2', function() {
	var factory = {};

	factory.multiply = function(a, b) {
		return a * b
	}
	return factory;
});

app.service('CalcService', function(MathService) {
	this.square = function(a) {
		return MathService.multiply(a, a);
	}
});

app.controller('CalcController', function(CalcService) {
	this.square = function() {
		this.result = CalcService.square(this.number);
	}
});
// End of Inject

function UserService_Service() {
	this.sayHello = function(name) {
		return 'Hello there ' + name;
	};
}

function UserService() {
	var UserService = {};
	function greeting(name) {
		return 'Hello there ' + name;
	}
	UserService.sayHello = function(name) {
		return greeting(name);
	};
	return UserService;
}
// End of service


// Services
app.service('UserService', UserService);

// Controllers
app.controller("demoCtrl", demoCtrl, ['$scope', 'myReusableSrvc1', 'myReusableSrvc2', function($scope, myReusableSrvc1, myReusableSrvc2) {
    // controller code
}]);

app.controller("userCtrl", userCtrl);

app.controller("itemCtrl", itemCtrl);

app.controller("servCtrl", servCtrl);

app.controller("ajaxCtrl", ajaxCtrl);

app.controller("studentController", studentController);

// Custom directives
app.directive("myTestDirective1", function() {
    return {
        template : "<h1>Made by a directive!</h1>"
    };
});

app.directive("myTestDirective", function() {
	// The legal restrict values are:
	// E for Element name
	// A for Attribute
	// C for Class
	// M for Comment
	// By default the value is EA
    return {
        restrict : "EA",
        template : "<h1>Made by a directive!</h1>"
    };
});


// Custom filters
app.filter('myFormat', function() {
    return function(x) {
        var i, c, txt = "";
        for (i = 0; i < x.length; i++) {
            c = x[i];
            if (i % 2 == 0) {
                c = c.toUpperCase();
            }
            txt += c;
        }
        return txt;
    };
});

// Use a Custom Service Inside a Filter
app.filter('myToHex',['hexafy', function(hexafy) {
    return function(x) {
        return hexafy.toHexString(x);
    };
}]);


