/**
 * Created by samuel on 02.11.16.
 */
var stomp = Stomp.over(new SockJS('/connect'));
stomp.debug = function (args) {
    console.log(args);
};
stomp.connect(header, function (frame) {
    console.log(frame);
    stomp.subscribe('/user/check', function (payload, headers, res) {
        console.log(payload);
        var event = JSON.parse(payload.body);
        if (event.message) {
            $('#log').append('</br>' + event.message);
        }
        executeEvent(event);
    }, header);
}, function (err) {
    console.error('connection error:' + err);
});

function executeEvent(event) {
    switch (event.typ) {
        case 'ResolverFinished':
            setResolvedResult(event.result);
            break;
        case 'TesterFinished':
            setResultMalicious(event.result);
            break;
        case 'TestFinished':
            setTestResult(event);
            break;
        default:
            break;
    }
}

function setTestResult(event) {
    switch (event.test_name) {
        case 'VirusScan':
            setVirusScanResult(event.result);
            break;
        case 'PortScan':
            setPortScanResult(event.result);
            break;
        case 'HeaderInspection':
            setHeaderInspectionResult(event.result);
            break;
        default:
            break;
    }
}

function setVirusScanResult(result) {
    setResultMaliciousImage($('#virusscan-state'), result.result);
    $('#virusscan-placeholder').addClass('invisible');
    $('#virusscan-info').html('Geprüfte Dateien: ' + result.info.scanned_files + '</br>Infizierte Dateien gefunden: ' + result.info.malicious_files).removeClass('invisible');
    var files = result.info.files;
    for (var i = 0; i < files.length; i++) {
        $('#virusscan-result').append('<tr' + (files[i].malicious ? ' class="table-danger"' : '') + '><td>' + files[i].name + '</td><td><span class="fa fa-' + (files[i].malicious ? 'exclamation-circle text-danger' : 'check-circle text-success') + '"></span></td></tr>');
    }
    $('#virusscan-result').removeClass('invisible');
}

function setPortScanResult(result) {
    setResultMaliciousImage($('#portscan-state'), result.result);
    $('#portscan-placeholder').addClass('invisible');
    $('#portscan-info').html('Keine verdächtigen Ports gefunden.').removeClass('invisible');
    var unknown_ports = result.info.unknown_ports;
    if (unknown_ports.length > 0) {
        $('#portscan-info').html('Folgende verdächtige Ports wurden abgefragt:');
        for (var i = 0; i < unknown_ports.length; i++) {
            $('#portscan-result').append('<tr><td>' + unknown_ports[i] + '</td></tr>');
        }
        $('#portscan-result').removeClass('invisible');
    }
}

function setHeaderInspectionResult(result) {
    setResultMaliciousImage($('#header-inspection-state'), result.result);
    $('#header-inspection-placeholder').addClass('invisible');
    var resultText = (result.info.malicious) ? "HTML-Antworten stimmen nicht überein" : "HTML-Antworten stimmen überein";
    $('#header-inspection-info').html(resultText).removeClass('invisible');
}

function setResolvedResult(result) {
    if (result.reachable) {
        $('#resolved-url').html(result.resolved);
    } else {
        // TODO show error and cancel all views
    }
}

function setResultMaliciousImage(element, result) {
    if (result.valueOf() == "CLEAN") {
        element.attr('src', 'img/clean.png');
    } else if (result.valueOf() == "WARNING")  {
        element.attr('src', 'img/warning.png');
    }else if (result.valueOf() == "MALICIOUS")  {
        element.attr('src', 'img/malicious.png');
    }else {
        element.attr('src', 'img/undefined.png');
    }
}

function setResultMalicious(result) {
    $('#heading-log').css('background-color', '#f5f5f5');
    if (result.valueOf() == "CLEAN") {
        $('#test-state').attr('src', 'img/webifier-clean.png');
    } else if (result.valueOf() == "WARNING")  {
        $('#test-state').attr('src', 'img/webifier-warning.png');
    }else if (result.valueOf() == "MALICIOUS")  {
        $('#test-state').attr('src', 'img/webifier-malicious.png');
    }else {
        $('#test-state').attr('src', 'img/webifier-undefined.png');
    }
}
