/**
 * Created by samuel on 02.11.16.
 */
var client = Stomp.over(new SockJS('/connect'));
client.connect(getHeader(), function (frame) {
    console.log(frame);
    client.subscribe('/user/started', function (payload) {
        $('#test-state').attr('src', 'img/webifier-loading.gif');
        $('#waiting-position').hide();
        $('#test-info').hide();
    }, getHeader());
    client.subscribe('/user/waiting', function (payload) {
        $('#waiting-position').html(payload.body);
    }, getHeader());
    client.subscribe('/user/check', function (payload) {
        var event = JSON.parse(payload.body);
        if (event.message) {
            $('#log').append('</br>' + event.message);
        }
        executeEvent(event);
    }, getHeader());
    client.send('/connect', header, 'connect');
}, function (err) {
    console.error('connection error:' + err);
});

function getHeader() {
    return JSON.parse(JSON.stringify(header));
}

function executeEvent(event) {
    switch (event.typ) {
        case 'ResolverFinished':
            setResolvedResult(event.result);
            break;
        case 'TestStarted':
            setTestLoading(event);
            break;
        case 'TestFinished':
            setTestResult(event);
            break;
        case 'TesterFinished':
            setResultMalicious(event.result);
            break;
        default:
            break;
    }
}

function setTestLoading(event) {
    switch (event.test_name) {
        case 'VirusScan':
            $('#virusscan-state').attr('src', 'img/loading.gif');
            break;
        case 'PortScan':
            $('#portscan-state').attr('src', 'img/loading.gif');
            break;
        case 'HeaderInspection':
            $('#header-inspection-state').attr('src', 'img/loading.gif');
            break;
        case 'LinkChecker':
            $('#linkchecker-state').attr('src', 'img/loading.gif');
            break;
        case 'CertificateChecker':
            $('#certificatechecker-state').attr('src', 'img/loading.gif');
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
        case 'LinkChecker':
            setLinkCheckerResult(event.result);
            break;
        case 'CertificateChecker':
            setCertificateCheckerResult(event.result);
            break;
        default:
            break;
    }
}

function setVirusScanResult(result) {
    setResultMaliciousImage($('#virusscan-state'), result.result);
    $('#virusscan-placeholder').addClass('invisible');
    $('#virusscan-info').html('Geprüfte Dateien: ' + result.info.scanned_files + '</br>Verdächtige Dateien: ' + result.info.suspicious_files + '</br>Maliziöse Dateien: ' + result.info.malicious_files).removeClass('invisible');
    var files = result.info.files;
    for (var i = 0; i < files.length; i++) {
        var table = '';
        var icon = 'fa-check-circle';
        var color = 'text-success';
        if (files[i].result == "SUSPICIOUS") {
            table = 'table-warning';
            icon = 'fa-exclamation-triangle';
            color = 'text-warning';
        }
        if (files[i].result == "MALICIOUS") {
            table = 'table-danger';
            icon = 'fa-times-circle';
            color = 'text-danger';
        }
        $('#virusscan-result').append('<tr class="' + table + '"><td><small>' + files[i].name + '</small></td><td><span class="fa ' + icon + ' ' + color + '"></span></td></tr>');
    }
    $('#virusscan-result').removeClass('invisible');
}

function setPortScanResult(result) {
    setResultMaliciousImage($('#portscan-state'), result.result);
    $('#portscan-placeholder').addClass('invisible');
    var $portscan_info = $('#portscan-info');
    $portscan_info.html('Keine verdächtigen Ports gefunden.').removeClass('invisible');
    var unknown_ports = result.info.unknown_ports;
    if (unknown_ports.length > 0) {
        $portscan_info.html('Folgende verdächtige Ports wurden abgefragt:');
        var $portscan_result = $('#portscan-result');
        for (var i = 0; i < unknown_ports.length; i++) {
            $portscan_result.append('<tr><td>' + unknown_ports[i] + '</td></tr>');
        }
        $portscan_result.removeClass('invisible');
    }
}

function setHeaderInspectionResult(result) {
    setResultMaliciousImage($('#header-inspection-state'), result.result);
    $('#header-inspection-placeholder').addClass('invisible');
    var resultText = (result.info.malicious) ? "HTML-Antworten stimmen nicht überein" : "HTML-Antworten stimmen überein";
    $('#header-inspection-info').html(resultText).removeClass('invisible');
}

function setLinkCheckerResult(result) {
    setResultMaliciousImage($('#linkchecker-state'), result.result);
    $('#linkchecker-placeholder').addClass('invisible');
    var hosts = result.info.hosts;
    $('#linkchecker-info').html((hosts.length > 0) ? 'Geprüfte Webseiten:' : 'Keine Links gefunden.').removeClass('invisible');
    for (var i = 0; i < hosts.length; i++) {
        var table = '';
        var icon = 'fa-check-circle';
        var color = 'text-success';
        if (hosts[i].result == "SUSPICIOUS") {
            table = 'table-warning';
            icon = 'fa-exclamation-triangle';
            color = 'text-warning';
        }
        if (hosts[i].result == "MALICIOUS") {
            table = 'table-danger';
            icon = 'fa-times-circle';
            color = 'text-danger';
        }
        if (hosts[i].result == "UNDEFINED") {
            icon = 'fa-question-circle';
            color = 'text-muted';
        }
        $('#linkchecker-result').append('<tr class="' + table + '"><td><small>' + hosts[i].host + '</small></td><td><span class="fa ' + icon + ' ' + color + '"></span></td></tr>');
    }
    $('#linkchecker-result').removeClass('invisible');
}

function setCertificateCheckerResult(result) {
    setResultMaliciousImage($('#certificatechecker-state'), result.result);
    $('#certificatechecker-placeholder').addClass('invisible');
    $('#certificatechecker-info').html(result.info ? 'Zertifikat:' : 'Kein Zertifikat gefunden!').removeClass('invisible');
    if (result.info) {
        if (result == 'MALICIOUS') {
            $('#certificatechecker-result').append($('<p>').css('font-weight', 'bold').html(result.info.certificate.result_code));
        }
        var certificate = result.info.certificate;
        var subject = $('<table>').addClass('table table-sm table-striped small');
        subject.append($('<tr>').append($('<th>').html('Name')).append($('<td>').html(certificate.subject.name)));
        subject.append($('<tr>').append($('<th>').html('Organisation')).append($('<td>').html(certificate.subject.organisation)));
        subject.append($('<tr>').append($('<th>').html('Organisationseinheit')).append($('<td>').html(certificate.subject.organisation_unit)));
        $('#certificatechecker-result').append($('<p>').addClass('small').html('Ausgestellt für:')).append(subject);
        var issuer = $('<table>').addClass('table table-sm table-striped small');
        issuer.append($('<tr>').append($('<th>').html('Name')).append($('<td>').html(certificate.issuer.name)));
        issuer.append($('<tr>').append($('<th>').html('Organisation')).append($('<td>').html(certificate.issuer.organisation)));
        issuer.append($('<tr>').append($('<th>').html('Organisationseinheit')).append($('<td>').html(certificate.issuer.organisation_unit)));
        $('#certificatechecker-result').append($('<p>').addClass('small').html('Ausgestellt von:')).append(issuer);
        var validity = $('<table>').addClass('table table-sm table-striped small');
        weekday: 'short',
        var options = {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        };
        var from = new Date(certificate.validity.from);
        validity.append($('<tr>').append($('<th>').html('Gültig ab')).append($('<td>').html(from.toLocaleString('de-DE', options))));
        var to = new Date(certificate.validity.to);
        validity.append($('<tr>').append($('<th>').html('Gültig bis')).append($('<td>').html(to.toLocaleString('de-DE', options))));
        $('#certificatechecker-result').append($('<p>').addClass('small').html('Gültigkeitszeitraum:')).append(validity);

        $('#certificatechecker-result').removeClass('invisible');
    }
}


function setResolvedResult(result) {
    if (result.reachable) {
        $('#resolved-url').html(result.resolved);
    } else {
        $('#resolved-url').html("Nicht erreichbar!");
        $('#test-state').attr('src', 'img/webifier.png');
    }
}

function setResultMaliciousImage(element, result) {
    element.attr('src', 'img/' + getResultImage(result));
}

function setResultMalicious(result) {
    $('#heading-log').css('background-color', '#f5f5f5');
    $("#favicon").attr('href', 'img/webifier-' + getResultImage(result));
    $('#test-state').attr('src', 'img/webifier-' + getResultImage(result));
}

function getResultImage(result) {
    switch (result) {
        case "CLEAN":
            return 'clean.png';
        case "SUSPICIOUS":
            return 'warning.png';
        case "MALICIOUS":
            return 'malicious.png';
        default:
            return 'undefined.png';
    }
}