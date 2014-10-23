package com.wlangiewicz.blockchainwriter

import java.io.File
import java.math.BigInteger

import org.bitcoinj.core._
import org.bitcoinj.net.discovery.DnsDiscovery
import org.bitcoinj.params.{MainNetParams, RegTestParams, TestNet3Params}
import org.bitcoinj.store.SPVBlockStore


object Main extends App{
  if (args.length < 2) {
    Console.err.println("Usage: private-key regtest|testnet|mainnet")
  }
  else{
    Console.println("Creating wallet")
    var filePrefix: String = null

    // Figure out which network we should connect to. Each one gets its own set of files.
    val params: NetworkParameters = args(1) match {
      case "mainnet" => {
        filePrefix = "forwarding-service-testnet"
        MainNetParams.get
      }
      case "testnet" => {
        filePrefix = "forwarding-service-testnet"
        TestNet3Params.get
      }
      case "regtest" => {
        filePrefix = "forwarding-service-regtest"
        RegTestParams.get
      }
    }

    // Parse the address given as the first parameter.
    val privateKey = addressToKey(params, args(0))

    val chainStore: SPVBlockStore = new SPVBlockStore(params, new File(filePrefix + ".spvchain"))
    val chain: BlockChain = new BlockChain(params, chainStore)

    val wallet = new Wallet(params)
    wallet.importKey(privateKey)

    val peers: PeerGroup = new PeerGroup(params, chain)
    peers.addPeerDiscovery(new DnsDiscovery(params))

    chain.addWallet(wallet)
    peers.addWallet(wallet)

    val blockChainListener: DownloadListener = new DownloadListener {
      override def doneDownload {
        Console.println("blockchain downloaded")
      }
    }

    peers.startAsync
    peers.awaitRunning
    peers.startBlockChainDownload(blockChainListener)
    blockChainListener.await

    Console.println(wallet.toString)

    peers.stopAsync
    peers.awaitTerminated

  }

  def addressToKey(params: NetworkParameters, sourceAddress: String): ECKey = {
    sourceAddress match {
      case _ if sourceAddress.length == 51 || sourceAddress.length == 52 => new DumpedPrivateKey(params, sourceAddress).getKey //WIF
      case _ if sourceAddress.length == 64 => ECKey.fromPrivate(new BigInteger(sourceAddress, 16)) //hex encoded private key
      case _ => ECKey.fromPrivate(Base58.decodeToBigInteger(sourceAddress)) //base58
    }
  }
}
