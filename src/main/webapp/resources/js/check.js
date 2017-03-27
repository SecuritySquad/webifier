/**
 * Created by samuel on 02.11.16.
 */
var client = Stomp.over(new SockJS('/connect'));
client.debug = function (message) {
//  no debug
};
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
            setResultImage(event.result);
            setAllRunningTestsUndefined();
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
        case 'IpScan':
            $('#ipscan-state').attr('src', 'img/loading.gif');
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
        case 'PhishingDetector':
            $('#phishingdetector-state').attr('src', 'img/loading.gif');
            break;
        case 'Screenshot':
            $('#screenshot-state').attr('src', 'img/loading.gif');
            break;
        case 'GoogleSafeBrowsing':
            $('#google-safe-browsing-state').attr('src', 'img/loading.gif');
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
        case 'IpScan':
            setIpScanResult(event.result);
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
        case 'PhishingDetector':
            setPhishingDetectorResult(event.result);
            break;
        case 'Screenshot':
            setScreenshotResult(event.result);
            break;
        case 'GoogleSafeBrowsing':
            setGoogleSafeBrowsingResult(event.result);
            break;
    }
}

function setScreenshotResult(result) {
    setSingleTestResultImage($('#screenshot-state'), result.result);
    $('#screenshot-placeholder').addClass('invisible');
    var base64img = result.info.base64img;
    $('#screenshot-block').append($('<img>').css({
        'display': 'block',
        'margin': '0 auto',
        'border': '1px solid rgba(0, 0, 0, 0.125)',
        'max-width': '100%'
    }).attr('src', base64img));
}

function setVirusScanResult(result) {
    setSingleTestResultImage($('#virusscan-state'), result.result);
    $('#virusscan-placeholder').addClass('invisible');
    $('#virusscan-info').html('Geprüfte Dateien: ' + result.info.scanned_files + '</br>Verdächtige Dateien: ' + result.info.suspicious_files + '</br>Maliziöse Dateien: ' + result.info.malicious_files).removeClass('invisible');
    var files = result.info.files;
    for (var i = 0; i < files.length; i++) {
        var table = '';
        if (files[i].result == "SUSPICIOUS")
            table = 'table-warning';
        if (files[i].result == "MALICIOUS")
            table = 'table-danger';
        $('#virusscan-result').append('<tr class="' + table + '"><td><small>' + files[i].name + '</small></td><td>' + getFontAwesomeResultSymbol(files[i].result) + '</td></tr>');
    }
    $('#virusscan-result').removeClass('invisible');
}

function setPortScanResult(result) {
    setSingleTestResultImage($('#portscan-state'), result.result);
    $('#portscan-placeholder').addClass('invisible');
    var $portscan_info = $('#portscan-info');
    $portscan_info.html('Keine verdächtigen Portabfragen gefunden.').removeClass('invisible');
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

function setIpScanResult(result) {
    setSingleTestResultImage($('#ipscan-state'), result.result);
    $('#ipscan-placeholder').addClass('invisible');
    var $ipscan_info = $('#ipscan-info');
    $ipscan_info.html('Keine verdächtigen IP-Abfragen gefunden.').removeClass('invisible');
    var risky_hosts = result.info.risky_hosts;
    if (risky_hosts.length > 0) {
        $ipscan_info.html('Folgende verdächtige IPs wurden abgefragt:');
        var $ipscan_result = $('#ipscan-result');
        for (var i = 0; i < risky_hosts.length; i++) {
            $ipscan_result.append('<tr><td>' + risky_hosts[i] + '</td></tr>');
        }
        $ipscan_result.removeClass('invisible');
    }
}

function setHeaderInspectionResult(result) {
    setSingleTestResultImage($('#header-inspection-state'), result.result);
    $('#header-inspection-placeholder').addClass('invisible');
    var info = $('<table>').addClass('table table-sm table-striped small');
    info.append($('<tr>').append($('<th>').html('Durchschnittliche Abweichung:'))
        .append($('<td>').html(result.info.medianDiff + ' Zeichen'))
        .append($('<td>').html(((1 - result.info.medianRatio) * 100).toFixed(2) + ' %')));
    info.append($('<tr>').append($('<th>').html('Maximale Abweichung:'))
        .append($('<td>').html(result.info.worstDiff + ' Zeichen'))
        .append($('<td>').html(((1 - result.info.worstRatio) * 100).toFixed(2) + ' %')));
    var list = $('<ul>').addClass('list-group small');
    var browsers = result.info.browsers;
    for (var i = 0; i < browsers.length; i++) {
        list.append($('<li>').addClass('list-group-item').html(browsers[i]));
    }
    $('#header-inspection-info').append(info).append($('<p>').html("Geprüfte Systeme:")).append(list).removeClass('invisible');
}

function setLinkCheckerResult(result) {
    setSingleTestResultImage($('#linkchecker-state'), result.result);
    $('#linkchecker-placeholder').addClass('invisible');
    var hosts = result.info.hosts;
    $('#linkchecker-info').html((hosts.length > 0) ? 'Geprüfte Webseiten:' : 'Keine Links gefunden.').removeClass('invisible');
    for (var i = 0; i < hosts.length; i++) {
        var table = '';
        if (hosts[i].result == "SUSPICIOUS")
            table = 'table-warning';
        if (hosts[i].result == "MALICIOUS")
            table = 'table-danger';
        $('#linkchecker-result').append('<tr class="' + table + '"><td><small>' + hosts[i].host + '</small></td><td>' + getFontAwesomeResultSymbol(hosts[i].result) + '</td></tr>');
    }
    $('#linkchecker-result').removeClass('invisible');
}

function setCertificateCheckerResult(result) {
    setSingleTestResultImage($('#certificatechecker-state'), result.result);
    $('#certificatechecker-placeholder').addClass('invisible');
    $('#certificatechecker-info').html((result.info && result.info.certificate) ? 'Zertifikat:' : 'Kein Zertifikat gefunden!').removeClass('invisible');
    if (result.info && result.info.certificate) {
        var certificate = result.info.certificate;
        if (result.result == 'MALICIOUS') {
            $('#certificatechecker-result').append($('<p>').css('font-weight', 'bold').html('Fehler: ' + certificate.return_code));
        }
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
        var options = {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        var from = new Date(certificate.validity.from);
        validity.append($('<tr>').append($('<th>').html('Gültig ab')).append($('<td>').html(from.toLocaleString('de-DE', options))));
        var to = new Date(certificate.validity.to);
        validity.append($('<tr>').append($('<th>').html('Gültig bis')).append($('<td>').html(to.toLocaleString('de-DE', options))));
        $('#certificatechecker-result').append($('<p>').addClass('small').html('Gültigkeitszeitraum:')).append(validity);

        $('#certificatechecker-result').removeClass('invisible');
    }
}

function setPhishingDetectorResult(result) {
    setSingleTestResultImage($('#phishingdetector-state'), result.result);
    $('#phishingdetector-placeholder').addClass('invisible');
    $('#phishingdetector-info').html('<b>Schlagwörter:</b> ' + result.info.keywords.join(', ')).removeClass('invisible');
    if (result.info.matches.length > 0) {
        $('#phishingdetector-result').append($('<p>').html('Übereinstimmungen:'));
        var matches = result.info.matches;
        for (var i = 0; i < matches.length; i++) {
            var match = matches[i];
            $('#phishingdetector-result').append($('<p>').addClass('small').html($('<a>').attr({
                'href': match.url,
                'target': '_blank'
            }).html(match.url)));
            var match_result = $('<table>').addClass('table table-sm table-striped small no-margin');
            match_result.append($('<tr>').append($('<th>').html('Resultat')).append($('<th>').html(getFontAwesomeResultSymbol(match.result))));
            match_result.append($('<tr>').append($('<th>').html('Übereinstimmung')).append($('<th>').html((match.ratio * 100).toFixed(2) + ' %')));
            match_result.append($('<tr>').append($('<td>').html('-> Inhalt')).append($('<td>').html((match.content_ratio * 100).toFixed(2) + ' %')));
            match_result.append($('<tr>').append($('<td>').html('-> Quelltext')).append($('<td>').html((match.html_ratio * 100).toFixed(2) + ' %')));
            match_result.append($('<tr>').append($('<td>').html('-> Aussehen')).append($('<td>').html((match.screenshot_ratio * 100).toFixed(2) + ' %')));
            $('#phishingdetector-result').append(match_result);
            var match_comparison = $('<div>').css({
                'width': '100%',
                'max-height': '400px',
                'overflow': 'auto',
                'border': '1px solid #eceeef',
                'margin-bottom': '1rem'
            }).html($('<img>').attr({
                'src': match.comparison
            }).css({
                'display': 'block',
                'margin': '0 auto',
                'width': '100%'
            }));
            $('#phishingdetector-result').append(match_comparison);
        }
        $('#phishingdetector-result').removeClass('invisible');
    }
}

function setGoogleSafeBrowsingResult(result) {
    setSingleTestResultImage($('#google-safe-browsing-state'), result.result);
    $('#google-safe-browsing-placeholder').addClass('invisible');
    var list = $('<ul>').addClass('list-group small');
    var matches = result.info.matches;
    if (matches.length > 0) {
        for (var i = 0; i < matches.length; i++) {
            list.append($('<li>').addClass('list-group-item').html(matches[i]));
        }
        $('#google-safe-browsing-info').append($('<p>').html("Gefundene Bedrohungen:")).append(list).removeClass('invisible');
    } else {
        $('#google-safe-browsing-info').append($('<p>').html("Keine Bedrohungen gefunden.")).removeClass('invisible');
    }
}

function getFontAwesomeResultSymbol(result) {
    var icon = 'fa-check-circle';
    var color = 'text-success';
    if (result == "SUSPICIOUS") {
        icon = 'fa-exclamation-triangle';
        color = 'text-warning';
    }
    if (result == "MALICIOUS") {
        icon = 'fa-times-circle';
        color = 'text-danger';
    }
    if (result == "UNDEFINED") {
        icon = 'fa-question-circle';
        color = 'text-muted';
    }
    return '<span class="fa ' + icon + ' ' + color + '"></span>';
}

function setResolvedResult(result) {
    if (result.reachable) {
        $('#resolved-url').html(result.resolved);
    } else {
        $('#resolved-url').html("Nicht erreichbar!");
        $('#test-state').attr('src', '/img/webifier.png');
    }
}

function setSingleTestResultImage(element, result) {
    element.attr('src', 'img/' + getResultImage(result));
}

function setResultImage(result) {
    $('#heading-log').css('background-color', '#f5f5f5');
    $("#favicon").attr('href', '/img/webifier-' + getResultImage(result));
    $('#test-state').attr('src', '/img/webifier-' + getResultImage(result));
}

function setAllRunningTestsUndefined() {
    $('img[src="/img/loading.gif"]').attr('src', '/img/undefined.png');
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